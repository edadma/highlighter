package io.github.edadma.highlighter

import scala.util.matching.Regex
import scala.collection.mutable

/** CharSequence wrapper that throws `InterruptedException` from `charAt`
  * when the current thread's interrupt flag is set. Wrapping the substring
  * passed to `Regex.findFirstMatchIn` makes the JVM matcher abort
  * mid-match instead of running forever — essential because some
  * VS Code TextMate patterns trigger catastrophic backtracking under
  * java.util.regex (which has no built-in match timeout). The check is
  * cheap (a single volatile read) and the substring is only walked
  * char-by-char by the regex engine, so the wrapper has no measurable
  * overhead in the happy path.
  *
  * On JS / Native, `Thread.interrupted()` is a no-op stub — `charAt`
  * just delegates. That's correct: those backends don't need interrupts
  * because they're not running in long-lived multi-threaded contexts
  * where one bad input would block other work. */
private[highlighter] final class InterruptibleCharSequence(s: CharSequence) extends CharSequence:
  def length: Int                             = s.length
  def subSequence(start: Int, end: Int)       = new InterruptibleCharSequence(s.subSequence(start, end))
  override def toString                       = s.toString
  def charAt(i: Int): Char =
    if Thread.interrupted() then throw new InterruptedException("highlight interrupted")
    s.charAt(i)

case class ResolvedPattern(
    name: Option[String],
    regex: Regex,
    isBegin: Boolean,
    endPattern: Option[String],
    // Pre-compiled end regex. Compiling at tokenize time on every state
    // push allocated thousands of Regex objects per line on complex
    // grammars (bash, JS) and OOMed long-running runs. Compiled once
    // during pattern resolution and reused.
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
// previous filler `(?!)` (empty negative lookahead) is fine on JVM but
// blows up on Scala Native's regex engine ("Unknown inline modifier") —
// so we use `\b\B`, a self-contradictory pair (word-boundary AND
// non-word-boundary at the same position) that compiles cleanly
// everywhere and is guaranteed to never match.
private[highlighter] val SelfMarker = ResolvedPattern(
  name = None, regex = "\\b\\B".r, isBegin = false,
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
  private val posixClasses = List(
    "[:alnum:]", "[:alpha:]", "[:digit:]", "[:lower:]", "[:upper:]",
    "[:space:]", "[:blank:]", "[:punct:]", "[:xdigit:]", "[:ascii:]",
  )

  private val repository: Map[String, RepositoryEntry] =
    grammar.repository.getOrElse(Map.empty)

  private val resolvedCache = mutable.Map[String, List[ResolvedPattern]]()
  // Snapshot of the prior pass's cache. Consulted ONLY when the current
  // resolution detects a cycle (visited.contains(key)) — instead of
  // returning Nil, we substitute whatever the previous pass managed to
  // resolve for that key. Successive passes thus monotonically grow each
  // cyclic key (a Nil from pass N becomes the partial-but-bigger result
  // from pass N's main resolution in pass N+1's cycle return), and the
  // cache reaches a fixed point in 2-3 passes on real grammars.
  private var fallbackCache: Map[String, List[ResolvedPattern]] = Map.empty

  // Diagnostics: every regex that fails to compile during pattern resolution
  // gets recorded here so the surrounding `Highlighter` can expose it via
  // `loadWarnings`. Without this, every Java-incompatible TextMate regex
  // (Oniguruma atomic groups, possessive quantifiers, etc.) silently
  // disappears and the user has no signal that the grammar is partially
  // dead. The buffer survives for the life of the Tokenizer.
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
  // on real grammars. We compare entry COUNTS rather than contents
  // because each pass freshly allocates Regex objects, and
  // java.util.regex.Pattern.equals is reference-based — value-based
  // comparison would always say "changed" and we'd run the cap forever.
  // Cyclic-key sizes only ever grow, so size equality is a sound
  // convergence test (total bytes can't grow without an entry growing).
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

  /** Bridge from Oniguruma-flavoured TextMate regex to java.util.regex.
    * VS Code grammars are written for Oniguruma, which is more permissive
    * than Java about a few constructs. Handle the cases that show up most
    * often in real grammars; leave Java-native syntax alone.
    *
    * Each step is idempotent so they compose freely.
    */
  private def preprocessRegex(pat: String): String =
    var result = pat
    // 1. POSIX character classes — Java accepts `\p{Alnum}` style, not
    //    `[:alnum:]`. Translation table lives in this file's helpers.
    for cls <- posixClasses do
      result = result.replace(cls, translatePosixClass(cls))
    // 2. Strip `(?#…)` comments. Oniguruma + Python accept them; Java
    //    treats `(?` as the start of a flag/group-modifier and chokes on
    //    `#`. Removing them is safe — they're prose for humans.
    result = stripInlineComments(result)
    // 3. Python-style named group `(?P<name>X)` → Java `(?<name>X)`. The
    //    leading `P` is the only spelling Java rejects.
    result = result.replace("(?P<", "(?<")
    // 4. `{,n}` (empty min) → `{0,n}`. Oniguruma + Perl accept the empty
    //    min; Java requires it explicit.
    result = fillEmptyQuantMin(result)
    // 5. Escape every `{` that isn't part of a valid `{n}` / `{n,}` /
    //    `{n,m}` quantifier. Without this, a literal `{` in a CSS rule
    //    or shell brace expansion explodes Java's regex parser.
    result = escapeLiteralBraces(result)
    result

  /** Strip `(?#…)` comment groups. Oniguruma / Python accept them as
    * inline comments inside a regex; java.util.regex does not. The body
    * may contain anything except a closing paren — there's no nested
    * paren handling because TextMate grammars don't nest comments. */
  private def stripInlineComments(pat: String): String =
    val sb = new StringBuilder
    var i = 0
    while i < pat.length do
      if i + 2 < pat.length && pat.charAt(i) == '(' && pat.charAt(i + 1) == '?' && pat.charAt(i + 2) == '#' then
        // Skip until matching `)`
        var j = i + 3
        while j < pat.length && pat.charAt(j) != ')' do j += 1
        if j < pat.length then i = j + 1
        else
          // Unterminated — just skip the marker; degenerate cases
          sb.append(pat.charAt(i))
          i += 1
      else
        sb.append(pat.charAt(i))
        i += 1
    sb.toString

  /** Rewrite `{,n}` (no lower bound) to `{0,n}`. Walks the string
    * character-by-character so the rule respects backslash escapes and
    * doesn't fire inside character classes by accident. */
  private def fillEmptyQuantMin(pat: String): String =
    val sb = new StringBuilder
    var i = 0
    while i < pat.length do
      val c = pat.charAt(i)
      if c == '\\' && i + 1 < pat.length then
        sb.append(c).append(pat.charAt(i + 1))
        i += 2
      else if c == '{' && i + 1 < pat.length && pat.charAt(i + 1) == ',' then
        // Look for `,N}` after the leading `,` — only rewrite if it
        // looks like a quantifier (digits then `}`).
        var j = i + 2
        while j < pat.length && pat.charAt(j).isDigit do j += 1
        if j < pat.length && pat.charAt(j) == '}' && j > i + 2 then
          sb.append("{0")
          i += 1 // step over the `{`, the `,` lands next pass
        else
          sb.append(c)
          i += 1
      else
        sb.append(c)
        i += 1
    sb.toString

  /** Escape literal `{` outside a valid quantifier. A valid quantifier is
    * `{n}` / `{n,}` / `{n,m}` where n and m are digits. Anything else —
    * `{`, `{x`, `{,`, `{abc}` — should be a literal `{`, not an error.
    *
    * Skip:
    *   - `\X` escapes in general (already escaped — leave the `{` alone)
    *   - `\p{Name}` / `\P{Name}` Unicode property escapes — those `{`
    *     are part of Java's required syntax, not a quantifier and not
    *     a literal we should escape
    *   - `\Q…\E` literal blocks — leave their interior alone (we don't
    *     emit these but a raw grammar might)
    */
  private def escapeLiteralBraces(pat: String): String =
    val sb = new StringBuilder
    var i = 0
    while i < pat.length do
      val c = pat.charAt(i)
      if c == '\\' && i + 1 < pat.length then
        val next = pat.charAt(i + 1)
        // Java-native braced escapes: `\p{Name}`, `\P{Name}` (Unicode
        // property), `\x{HHHH}` (hex code point). All three use `{...}`
        // as syntax, not as literal braces — copy them through verbatim.
        if (next == 'p' || next == 'P' || next == 'x') &&
            i + 2 < pat.length && pat.charAt(i + 2) == '{' then
          var j = i + 3
          while j < pat.length && pat.charAt(j) != '}' do j += 1
          if j < pat.length then
            sb.append(pat.substring(i, j + 1))
            i = j + 1
          else
            // Unterminated — pass through, let Java complain.
            sb.append(pat.charAt(i)).append(next)
            i += 2
        else
          sb.append(c).append(next)
          i += 2
      else if c == '{' then
        // Probe forward to see if this looks like a valid quantifier.
        var j = i + 1
        val firstDigitStart = j
        while j < pat.length && pat.charAt(j).isDigit do j += 1
        val hasFirstDigits = j > firstDigitStart
        var valid = false
        if hasFirstDigits then
          if j < pat.length && pat.charAt(j) == '}' then
            valid = true // {n}
          else if j < pat.length && pat.charAt(j) == ',' then
            j += 1
            while j < pat.length && pat.charAt(j).isDigit do j += 1
            if j < pat.length && pat.charAt(j) == '}' then valid = true // {n,} or {n,m}
        if valid then
          sb.append(c)
          i += 1
        else
          sb.append("\\{")
          i += 1
      else
        sb.append(c)
        i += 1
    sb.toString

  /** Compile `pat` to a [[Regex]]. On failure, record the original (pre-
    * processed) source plus the engine's complaint in [[loadWarnings]]
    * and return `None`. The caller (resolveOne) drops the pattern so the
    * rest of the grammar still loads — the alternative would be to fail
    * the whole `fromJson`, which loses many usable patterns to one bad
    * one in a 6000-line VS Code grammar. The warning list lets callers
    * surface the breakage instead of silently swallowing it. */
  private def compileRegex(pat: String): Option[Regex] =
    val processed = preprocessRegex(pat)
    try Some(new Regex(processed))
    catch case e: Exception =>
      warningsBuf += s"regex compile failed: $pat — ${e.getMessage}"
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
            // Pre-compile the end regex once. If end is missing or fails
            // to compile, the state pushed will have no end and never
            // pop on its own — same behaviour as before, just no per-
            // token-position re-compilation.
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
      m: Regex.Match,
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
              val gs = m.start(n)
              val ge = m.end(n)
              // Filter out captures that fall outside the visible match
              // window. A lookahead like `(?=...)` produces capture
              // groups whose offsets sit AFTER `m.end`; substringing
              // `matchedText` with those would throw. Same for lookbehind
              // captures (rare, but possible) which sit before `m.start`.
              if gs >= 0 && ge >= 0 && gs >= m.start && ge <= m.end then
                entry.name.foreach { name =>
                  segments += ((gs, ge, baseScopes ++ patternName.toList ++ List(name)))
                }
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
      // Wrap once per token-position so every regex matcher uses the
      // interruptible variant. See InterruptibleCharSequence's comment
      // for why this is necessary on JVM with java.util.regex.
      val sub = new InterruptibleCharSequence(line.substring(pos))

      // Try end pattern of current state
      val endResult = currentFrame.endRegex.flatMap { er =>
        er.findFirstMatchIn(sub).map { m =>
          (m, m.start + pos, m.end + pos)
        }
      }

      // Try all active patterns — find earliest match
      var bestPat: ResolvedPattern = null
      var bestMatch: Regex.Match = null
      var bestStart = Int.MaxValue

      for rp <- currentFrame.patterns if !rp.isSelfMarker do
        rp.regex.findFirstMatchIn(sub) match
          case Some(m) if m.start + pos < bestStart =>
            bestPat = rp
            bestMatch = m
            bestStart = m.start + pos
          case _ => ()

      // End pattern wins ties
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
        val matchEnd = bestMatch.end + pos

        if matchStart > pos then
          tokens += Token(line.substring(pos, matchStart), scopesFromStack(stateStack))

        // Guard against zero-length matches causing infinite loops
        if matchEnd == pos && !bestPat.isBegin then
          tokens += Token(line.substring(pos, pos + 1), scopesFromStack(stateStack))
          pos += 1
        else if !bestPat.isBegin then
          // Simple match — emit with captures
          emitWithCaptures(
            bestMatch.matched, bestMatch, pos, bestPat.captures,
            scopesFromStack(stateStack), bestPat.name, tokens,
          )
          pos = matchEnd
        else
          // Begin/end — emit begin with captures, push state
          emitWithCaptures(
            bestMatch.matched, bestMatch, pos, bestPat.captures,
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
