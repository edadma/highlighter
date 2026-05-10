package io.github.edadma.highlighter

import io.github.edadma.oniguruma.{Regex, Match => OnigMatch}
import scala.collection.mutable

case class ResolvedPattern(
    name: Option[String],
    regex: Regex,
    isBegin: Boolean,
    endPattern: Option[String],
    // Pre-compiled end regex. Compiling at tokenize time on every state
    // push allocated thousands of Regex objects per line on complex
    // grammars (bash, JS) and noticeably hurt longer runs. Compiled
    // once during pattern resolution and reused.
    endRegex: Option[Regex],
    endCaptures: Option[Map[String, CaptureEntry]],
    captures: Option[Map[String, CaptureEntry]], // match captures or beginCaptures
    innerPatternKey: Option[String],
    contentName: Option[String],
    isSelfMarker: Boolean = false,
)

// Sentinel pattern for $self includes — resolved lazily at tokenization
// time. The `regex` field is filler: every consumer filters with
// `isSelfMarker` first so the pattern is never matched against. The
// `\b\B` body (word-boundary AND non-word-boundary at the same
// position) compiles cleanly under both java.util.regex and oniguruma
// and is guaranteed never to match — safe filler.
private[highlighter] val SelfMarker = ResolvedPattern(
  name = None, regex = Regex("\\b\\B"), isBegin = false,
  endPattern = None, endRegex = None, endCaptures = None, captures = None,
  innerPatternKey = None, contentName = None, isSelfMarker = true,
)

case class StateFrame(
    patterns: List[ResolvedPattern],
    endRegex: Option[Regex],
    endCaptures: Option[Map[String, CaptureEntry]],
    scopeName: Option[String],
    contentName: Option[String],
)

class Tokenizer(grammar: Grammar):

  private val repository: Map[String, RepositoryEntry] =
    grammar.repository.getOrElse(Map.empty)

  private val resolvedCache = mutable.Map[String, List[ResolvedPattern]]()
  // Snapshot of the prior pass's cache. Consulted ONLY when the current
  // resolution detects a cycle (visited.contains(key)) — instead of
  // returning Nil, we substitute whatever the previous pass managed to
  // resolve for that key. Successive passes thus monotonically grow each
  // cyclic key, and the cache reaches a fixed point in 2-3 passes on
  // real grammars.
  private var fallbackCache: Map[String, List[ResolvedPattern]] = Map.empty

  // Diagnostics: every regex that fails to compile during pattern
  // resolution gets recorded here so the surrounding `Highlighter` can
  // expose it via `loadWarnings`. The buffer survives for the life of
  // the Tokenizer. With the oniguruma engine the warning list should
  // be empty for every TextMate grammar in the wild — the engine was
  // built to support exactly that corpus.
  private val warningsBuf = mutable.ListBuffer[String]()

  def loadWarnings: List[String] = warningsBuf.toList

  // resolveAndCache MUST run after warningsBuf is initialized — Scala
  // initializes vals top-to-bottom, so this assignment lives below the
  // buffer declaration on purpose. Don't reorder.
  //
  // Fixed-point iteration: each pass treats the previous pass's cache
  // as a fallback for cycle returns. Cyclic keys grow monotonically
  // (a Nil cycle return becomes the prior pass's partial value, which
  // is at least as large), so the iteration converges in 2-3 passes
  // on real grammars. Compare entry COUNTS, not contents — Regex
  // objects use reference identity, so value-based comparison would
  // always say "changed" and we'd run the cap forever. Cyclic-key
  // sizes only ever grow, so size equality is a sound convergence test.
  private val topPatterns: List[ResolvedPattern] =
    def sizeMap: Map[String, Int] =
      resolvedCache.map { case (k, v) => k -> v.length }.toMap
    var iters = 0
    var done = false
    var beforeSizes: Map[String, Int] = Map.empty
    while !done && iters < 6 do
      iters += 1
      fallbackCache = resolvedCache.toMap
      resolvedCache.clear()
      resolveAndCache("$top", grammar.patterns, Set.empty)
      val after = sizeMap
      done = after == beforeSizes
      beforeSizes = after
    fallbackCache = Map.empty
    resolvedCache("$top")

  /** Compile `pat` to a [[Regex]]. On failure, record the source plus
    * the engine's complaint in [[loadWarnings]] and return `None`. The
    * caller (resolveOne) drops the pattern so the rest of the grammar
    * still loads — the alternative would be to fail the whole
    * `fromJson`, which loses many usable patterns to one bad one in a
    * 6000-line VS Code grammar. */
  private def compileRegex(pat: String): Option[Regex] =
    Regex.compile(pat) match
      case Right(r) => Some(r)
      case Left(msg) =>
        warningsBuf += s"regex compile failed: $pat — $msg"
        None

  /** `visited` is the chain of keys currently being resolved; passing it
    * through (rather than resetting to `Set(key)`) lets cycle detection
    * extend across nested begin/end inner-pattern resolutions, so a deep
    * chain that loops back to an outer key short-circuits to Nil instead
    * of recursing forever. The key itself is added to visited inside the
    * resolution call (via `resolveOne`'s `newVisited`), so nothing is
    * lost. */
  private def resolveAndCache(key: String, patterns: List[Pattern], visited: Set[String]): List[ResolvedPattern] =
    resolvedCache.get(key) match
      case Some(cached) => cached
      case None =>
        val result = resolvePatterns(patterns, visited + key)
        resolvedCache(key) = result
        result

  private def resolvePatterns(patterns: List[Pattern], visited: Set[String]): List[ResolvedPattern] =
    patterns.flatMap(resolveOne(_, visited))

  private def resolveOne(p: Pattern, visited: Set[String]): List[ResolvedPattern] =
    p.include match
      case Some("$self") =>
        // Return marker — resolved lazily at tokenization time
        List(SelfMarker)

      case Some(ref) if ref.startsWith("#") =>
        val key = ref.drop(1)
        if visited.contains(key) then
          // Cycle: substitute the previous pass's resolution if any. This
          // is what makes fixed-point iteration converge — see
          // `fallbackCache` for the rationale.
          fallbackCache.getOrElse(key, Nil)
        else
          resolvedCache.get(key) match
            case Some(cached) => cached
            case None =>
              repository.get(key) match
                case Some(entry) =>
                  val newVisited = visited + key
                  val pat = RepositoryEntry.toPattern(entry)
                  val result = resolveOne(pat, newVisited)
                  resolvedCache(key) = result
                  result
                case None => Nil

      case Some(_) => Nil

      case None =>
        if p.`match`.isDefined then
          compileRegex(p.`match`.get).toList.map { r =>
            ResolvedPattern(p.name, r, isBegin = false, None, None, None, p.captures, None, None)
          }
        else if p.begin.isDefined then
          compileRegex(p.begin.get).toList.map { r =>
            val innerKey = s"inner_${System.identityHashCode(p)}"
            p.patterns.foreach(ps => resolveAndCache(innerKey, ps, visited))
            // Pre-compile the end regex once.
            val endRegex = p.end.flatMap(compileRegex)
            ResolvedPattern(
              p.name, r, isBegin = true, p.end, endRegex, p.endCaptures,
              p.beginCaptures.orElse(p.captures), Some(innerKey), p.contentName,
            )
          }
        else if p.patterns.isDefined then
          resolvePatterns(p.patterns.get, visited)
        else
          Nil

  def tokenize(text: String): List[List[Token]] =
    val lines = text.split("\n", -1).toList
    var stateStack: List[StateFrame] = List(
      StateFrame(topPatterns, endRegex = None, endCaptures = None, scopeName = Some(grammar.scopeName), contentName = None)
    )
    lines.map { line =>
      val (tokens, newStack) = tokenizeLine(line, stateStack)
      stateStack = newStack
      tokens
    }

  private def scopesFromStack(stateStack: List[StateFrame]): List[String] =
    stateStack.reverse.flatMap(f => f.scopeName.toList ++ f.contentName.toList)

  private def emitWithCaptures(
      matchedText: String,
      m: OnigMatch,
      baseOffset: Int,
      captures: Option[Map[String, CaptureEntry]],
      baseScopes: List[String],
      patternName: Option[String],
      tokens: mutable.ListBuffer[Token],
  ): Unit =
    captures match
      case Some(caps) if caps.nonEmpty =>
        // Build a list of (start, end, scopes) for each capture group
        val segments = mutable.ListBuffer[(Int, Int, List[String])]()

        // Check for group 0 (whole match scope)
        val group0Scope = caps.get("0").flatMap(_.name)
        val effectiveScopes = baseScopes ++ patternName.toList ++ group0Scope.toList

        for (key, entry) <- caps do
          key.toIntOption match
            case Some(n) if n > 0 && n <= m.groupCount =>
              m.groupRange(n) match
                case Some((gs, ge)) =>
                  // Filter captures that fall outside the visible match window.
                  // Lookahead capture groups can sit past `m.end`; substringing
                  // `matchedText` with those would throw. Same for lookbehind
                  // captures sitting before `m.start`.
                  if gs >= m.start && ge <= m.end then
                    entry.name.foreach { name =>
                      segments += ((gs, ge, baseScopes ++ patternName.toList ++ List(name)))
                    }
                case None => ()
            case _ => ()

        if segments.isEmpty then
          // Only group 0 or no valid captures
          tokens += Token(matchedText, effectiveScopes)
        else
          // Sort segments by start position, emit with gaps
          val sorted = segments.sortBy(_._1).toList
          var pos = 0
          for (gs, ge, scopes) <- sorted do
            val relStart = math.min(gs - m.start, matchedText.length)
            val relEnd   = math.min(ge - m.start, matchedText.length)
            if relStart > pos then
              tokens += Token(matchedText.substring(pos, relStart), effectiveScopes)
            if relEnd > relStart then
              tokens += Token(matchedText.substring(relStart, relEnd), scopes)
              pos = relEnd
          if pos < matchedText.length then
            tokens += Token(matchedText.substring(pos), effectiveScopes)

      case _ =>
        // No captures — emit whole match
        val scopes = baseScopes ++ patternName.toList
        tokens += Token(matchedText, scopes)

  private def tokenizeLine(line: String, initialStack: List[StateFrame]): (List[Token], List[StateFrame]) =
    val tokens = mutable.ListBuffer[Token]()
    var pos = 0
    var stateStack = initialStack

    while pos < line.length do
      val currentFrame = stateStack.head

      // Try end pattern of current state — scan forward from pos.
      val endResult = currentFrame.endRegex.flatMap { er =>
        er.matchAt(line, pos, pos) match
          case some @ Some(_) => some.map(m => (m, m.start, m.end))
          case None =>
            // matchAt is anchored. For end, we need to scan forward —
            // try positions one codepoint at a time until we find the
            // next match or run off the end. Iterate by code point so
            // supplementary chars stay atomic.
            var p = pos
            var found: Option[(OnigMatch, Int, Int)] = None
            while p < line.length && found.isEmpty do
              val cp = line.codePointAt(p)
              p += Character.charCount(cp)
              er.matchAt(line, p, pos) match
                case Some(m) => found = Some((m, m.start, m.end))
                case None    => ()
            found
      }

      // Try all active patterns at pos (anchored). The FIRST one that
      // matches wins — TextMate spec: list order is priority.
      var bestPat: ResolvedPattern = null
      var bestMatch: OnigMatch = null
      var i = 0
      val patsArr = currentFrame.patterns.toArray
      while bestPat == null && i < patsArr.length do
        val rp = patsArr(i)
        if !rp.isSelfMarker then
          rp.regex.matchAt(line, pos, pos) match
            case Some(m) =>
              bestPat = rp
              bestMatch = m
            case None => ()
        i += 1

      // If nothing anchored at pos, find next position any pattern fires
      // (scan-forward optimization so we don't try every pattern at every
      // char). Iterate by codepoint.
      if bestPat == null then
        var p = pos
        var earliestStart = Int.MaxValue
        var earliestPat: ResolvedPattern = null
        var earliestMatch: OnigMatch = null
        // Walk codepoint-by-codepoint. Cap the search by emitting a
        // single gap and looping when found; if nothing fires, the
        // whole rest of the line is plain text.
        while p < line.length && earliestPat == null do
          val cp = line.codePointAt(p)
          p += Character.charCount(cp)
          var j = 0
          while j < patsArr.length && earliestPat == null do
            val rp = patsArr(j)
            if !rp.isSelfMarker then
              rp.regex.matchAt(line, p, pos) match
                case Some(m) =>
                  earliestPat = rp
                  earliestMatch = m
                  earliestStart = p
                case None => ()
            j += 1
        if earliestPat != null then
          bestPat = earliestPat
          bestMatch = earliestMatch

      val bestStart = if bestPat != null then bestMatch.start else Int.MaxValue

      // End pattern wins ties — at-or-before-best.
      val useEnd = endResult match
        case Some((_, endStart, _)) => bestPat == null || endStart <= bestStart
        case None                   => false

      if useEnd then
        val (endM, endStart, endEnd) = endResult.get
        if endStart > pos then
          tokens += Token(line.substring(pos, endStart), scopesFromStack(stateStack))
        if endM.matched.nonEmpty then
          emitWithCaptures(
            endM.matched, endM, pos, currentFrame.endCaptures,
            scopesFromStack(stateStack.tail), stateStack.head.scopeName, tokens,
          )
        pos = endEnd
        stateStack = stateStack.tail

      else if bestPat != null then
        val matchStart = bestStart
        val matchEnd = bestMatch.end

        if matchStart > pos then
          tokens += Token(line.substring(pos, matchStart), scopesFromStack(stateStack))

        // Guard against zero-length matches causing infinite loops
        if matchEnd == matchStart && !bestPat.isBegin then
          tokens += Token(line.substring(matchStart, matchStart + 1), scopesFromStack(stateStack))
          pos = matchStart + 1
        else if !bestPat.isBegin then
          // Simple match — emit with captures
          emitWithCaptures(
            bestMatch.matched, bestMatch, matchStart, bestPat.captures,
            scopesFromStack(stateStack), bestPat.name, tokens,
          )
          pos = matchEnd
        else
          // Begin/end — emit begin with captures, push state
          emitWithCaptures(
            bestMatch.matched, bestMatch, matchStart, bestPat.captures,
            scopesFromStack(stateStack), bestPat.name, tokens,
          )
          val endRegex = bestPat.endRegex
          val rawInner = bestPat.innerPatternKey
            .flatMap(resolvedCache.get)
            .getOrElse(Nil)
          // Replace $self markers with the fully resolved top-level patterns
          val innerPatterns = rawInner.flatMap { rp =>
            if rp.isSelfMarker then topPatterns else List(rp)
          }
          stateStack = StateFrame(
            patterns = innerPatterns,
            endRegex = endRegex,
            endCaptures = bestPat.endCaptures,
            scopeName = bestPat.name,
            contentName = bestPat.contentName,
          ) :: stateStack
          pos = matchEnd
      else
        tokens += Token(line.substring(pos), scopesFromStack(stateStack))
        pos = line.length

    (tokens.toList, stateStack)
