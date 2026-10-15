package io.github.edadma.highlighter

import io.github.edadma.oniguruma.{Regex, Match => OnigMatch}
import scala.collection.mutable

// Cutting text into tokens, line by line, with a stack of the `begin`/`end` pairs still open carried from one line
// to the next -- the algorithm of vscode-textmate, the tokenizer every one of these grammars is written and tested
// against in Visual Studio Code.
//
// **At each position the next thing to happen is whichever matches first**: the open pair's `end`, or the earliest
// of the rules in force there, the lower-numbered rule winning a tie and the `end` winning a tie with any rule
// (losing it, under `applyEndPatternLast`).
//
// **A line is matched with its newline**, since grammars close rules on `\n` -- `(?=;|&|\n)`, a Python string's
// `(\2)|(\n)` -- and the newline is cut off the tokens afterwards.
//
// **`\G` holds where the last `begin` match ended**, goes back to what it was when that pair closes, and holds
// nowhere at the start of a line -- unless the open pair's `begin` match ran to the end of the line before, when it
// holds at the new line's start.

case class ResolvedPattern(
    name: Option[String],
    regex: Regex,
    isBegin: Boolean,
    endPattern: Option[String],
    // Pre-compiled end regex. Compiling at tokenize time on every state
    // push allocated thousands of Regex objects per line on complex
    // grammars (bash, JS) and noticeably hurt longer runs. Compiled
    // once during pattern resolution and reused. `None` where the end
    // names the begin's groups (`endRefers`): it is compiled per match.
    endRegex: Option[Regex],
    endCaptures: Option[Map[String, CaptureEntry]],
    captures: Option[Map[String, CaptureEntry]], // match captures or beginCaptures
    innerPatternKey: Option[String],
    contentName: Option[String],
    isSelfMarker: Boolean = false,
    source: String = "",
    endRefers: Boolean = false,
    endLast: Boolean = false,
):
  // The same rule with every `\A` made to match nothing, for every position but the document's first.
  lazy val regexNotFirst: Regex =
    val rewritten = Tokenizer.withoutStartAnchor(source)
    if rewritten == source then regex
    else Regex.compile(rewritten).getOrElse(regex)

// Sentinel pattern for $self includes -- resolved lazily at tokenization
// time. The `regex` field is filler: every consumer filters with
// `isSelfMarker` first so the pattern is never matched against. The
// `\b\B` body (word-boundary AND non-word-boundary at the same
// position) compiles cleanly under both java.util.regex and oniguruma
// and is guaranteed never to match -- safe filler.
private[highlighter] val SelfMarker = ResolvedPattern(
  name = None, regex = Regex("\\b\\B"), isBegin = false,
  endPattern = None, endRegex = None, endCaptures = None, captures = None,
  innerPatternKey = None, contentName = None, isSelfMarker = true,
)

// One open `begin`/`end` pair, the grammar itself at the bottom of the stack, or a capture whose text is being
// tokenized again.
private[highlighter] case class Frame(
    // The rules in force inside, and the pair's identity.
    key: String,
    end: Option[Regex],
    endCaptures: Option[Map[String, CaptureEntry]],
    endLast: Boolean,
    // The scopes of the pair's own delimiters, and of the text between them -- the same, with the `contentName`
    // added.
    scopes: List[String],
    content: List[String],
    // Where on its line the pair was entered, and where `\G` held then; both forgotten at a new line.
    enter: Int,
    anchor: Int,
    // The `begin` match ran to the end of its line.
    eol: Boolean,
)

// The tokens of one line as they are produced: each one runs from where the last ended.
private final class Producer:
  val tokens: mutable.ListBuffer[Token] = mutable.ListBuffer()
  var last                              = 0

  // The text from the last token's end to `end`, under `scopes`. Nothing where `end` is not past it.
  def produce(line: String, scopes: List[String], end: Int): Unit =
    if end > last then
      tokens += Token(line.substring(last, end), scopes)
      last = end

class Tokenizer(grammar: Grammar):
  import Tokenizer.*

  // A position no search starts at, for "nowhere".
  private val NoPos = Int.MaxValue

  private val repository: Map[String, RepositoryEntry] =
    grammar.repository.getOrElse(Map.empty)

  // Each entry as a pattern, made once: a `begin` rule's inner patterns are filed under a key taken from its
  // pattern's identity, which must be the same object in every resolution pass.
  private val repositoryPatterns: Map[String, Pattern] =
    repository.map((k, v) => k -> RepositoryEntry.toPattern(v))

  private val resolvedCache = mutable.Map[String, List[ResolvedPattern]]()
  // Snapshot of the prior pass's cache. Consulted ONLY when the current
  // resolution detects a cycle (visited.contains(key)) -- instead of
  // returning Nil, we substitute whatever the previous pass managed to
  // resolve for that key. Successive passes thus monotonically grow each
  // cyclic key, and the cache reaches a fixed point in 2-3 passes on
  // real grammars.
  private var fallbackCache: Map[String, List[ResolvedPattern]] = Map.empty

  // Diagnostics: every regex that fails to compile during pattern
  // resolution gets recorded here so the surrounding `Highlighter` can
  // expose it via `loadWarnings`. The buffer survives for the life of
  // the Tokenizer. With the oniguruma engine the warning list should
  // be empty for every TextMate grammar in the wild -- the engine was
  // built to support exactly that corpus.
  private val warningsBuf = mutable.ListBuffer[String]()

  def loadWarnings: List[String] = warningsBuf.toList

  // Ends that name their begin's groups, compiled once per distinct result.
  private val endsCache = mutable.Map[String, Option[Regex]]()

  // The live rules under a key (every `$self` marker expanded), computed on first use.
  private val liveCache = mutable.Map[String, Array[ResolvedPattern]]()

  // resolveAndCache MUST run after warningsBuf is initialized -- Scala
  // initializes vals top-to-bottom, so this assignment lives below the
  // buffer declaration on purpose. Don't reorder.
  //
  // Fixed-point iteration: each pass treats the previous pass's cache
  // as a fallback for cycle returns. Cyclic keys grow monotonically
  // (a Nil cycle return becomes the prior pass's partial value, which
  // is at least as large), so the iteration converges in 2-3 passes
  // on real grammars. Compare entry COUNTS, not contents -- Regex
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
    * still loads -- the alternative would be to fail the whole
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

  // The rules of every capture that has `patterns`, filed under the capture's own key.
  private def captureLists(caps: Option[Map[String, CaptureEntry]], visited: Set[String]): Unit =
    caps.foreach(_.values.foreach(c => c.patterns.foreach(ps => resolveAndCache(captureKey(c), ps, visited))))

  private def resolveOne(p: Pattern, visited: Set[String]): List[ResolvedPattern] =
    p.include match
      // `$base` is the grammar a document is highlighted with, which for a grammar on its own is this one.
      case Some("$self") | Some("$base") =>
        // Return marker -- resolved lazily at tokenization time
        List(SelfMarker)

      case Some(ref) if ref.startsWith("#") =>
        val key = ref.drop(1)
        if visited.contains(key) then
          // Cycle: substitute the previous pass's resolution if any. This
          // is what makes fixed-point iteration converge -- see
          // `fallbackCache` for the rationale.
          fallbackCache.getOrElse(key, Nil)
        else
          resolvedCache.get(key) match
            case Some(cached) => cached
            case None =>
              repositoryPatterns.get(key) match
                case Some(pat) =>
                  val newVisited = visited + key
                  val result = resolveOne(pat, newVisited)
                  resolvedCache(key) = result
                  result
                case None => Nil

      case Some(_) => Nil

      case None =>
        if p.`match`.isDefined then
          val src = p.`match`.get
          compileRegex(src).toList.map { r =>
            captureLists(p.captures, visited)
            ResolvedPattern(p.name, r, isBegin = false, None, None, None, p.captures, None, None, source = src)
          }
        else if p.begin.isDefined then
          val src = p.begin.get
          compileRegex(src).toList.map { r =>
            val innerKey = s"inner_${System.identityHashCode(p)}"
            p.patterns.foreach(ps => resolveAndCache(innerKey, ps, visited))
            // `end` is compiled once, unless it names the begin match's groups.
            val refers   = p.end.exists(hasBackReferences)
            val endRegex = if refers then None else p.end.flatMap(compileRegex)
            // `captures` stands for whichever of `beginCaptures` and `endCaptures` is absent.
            val beginCaps = p.beginCaptures.orElse(p.captures)
            val endCaps   = p.endCaptures.orElse(p.captures)
            captureLists(beginCaps, visited)
            captureLists(endCaps, visited)
            ResolvedPattern(
              p.name, r, isBegin = true, p.end, endRegex, endCaps, beginCaps, Some(innerKey), p.contentName,
              source = src, endRefers = refers, endLast = p.endLast,
            )
          }
        else if p.patterns.isDefined then
          resolvePatterns(p.patterns.get, visited)
        else
          Nil

  // The rules in force under a key, as matched: every `$self` marker expanded.
  private def rulesFor(key: String): Array[ResolvedPattern] =
    liveCache.getOrElseUpdate(
      key,
      (if key == "$top" then topPatterns else resolvedCache.getOrElse(key, Nil))
        .flatMap(rp => if rp.isSelfMarker then topPatterns.filterNot(_.isSelfMarker) else List(rp))
        .toArray,
    )

  def tokenize(text: String): List[List[Token]] =
    val stack = mutable.ArrayBuffer[Frame](
      Frame("$top", None, None, false, List(grammar.scopeName), List(grammar.scopeName), NoPos, NoPos, false)
    )
    var first = true
    text.split("\n", -1).toList.map { line =>
      // What a pair remembers of the line it was entered on means nothing on this one.
      for i <- stack.indices do stack(i) = stack(i).copy(enter = NoPos, anchor = NoPos)

      val anchor = if stack.last.eol then 0 else NoPos
      val p      = new Producer
      run(line + "\n", stack, 0, anchor, first, p)
      first = false
      clip(p.tokens.toList, line.length)
    }

  // Tokens from `start` to the end of `line`, under `stack`, which is left as the line leaves it. `atStart` says the
  // document's first line is being matched and nothing on it has been yet.
  private def run(
      line: String,
      stack: mutable.ArrayBuffer[Frame],
      start: Int,
      firstAnchor: Int,
      atStart: Boolean,
      out: Producer,
  ): Unit =
    var pos     = start
    var anchor  = firstAnchor
    var first   = atStart
    var running = true

    // Steps taken without moving. Every step that does not move pushes a pair not already open at this position,
    // or closes one entered before it, so the count is bounded by the grammar; this stops a grammar that defeats
    // that reasoning anyway.
    var stalled = 0

    while running do
      val frame = stack.last

      next(frame, line, pos, anchor, first) match
        case None =>
          out.produce(line, frame.content, line.length)
          running = false

        case Some((rule, m)) =>
          val advanced = m.end > pos

          if advanced then stalled = 0 else stalled += 1

          if stalled > 10000 then
            out.produce(line, frame.content, line.length)
            running = false
          else
            rule match
              case None =>
                // The pair closes.
                out.produce(line, frame.content, m.start)
                captures(line, frame.scopes, frame.endCaptures, m, stack, first, out)
                out.produce(line, frame.scopes, m.end)
                stack.remove(stack.length - 1)
                anchor = frame.anchor

                // It opened here and closed here, having matched nothing: it stays open, its `contentName` set
                // aside as the closing set it aside, and the rest of the line takes the pair's own scopes.
                if !advanced && frame.enter == pos then
                  stack += frame.copy(content = frame.scopes)
                  out.produce(line, frame.scopes, line.length)
                  running = false

              case Some(r) =>
                out.produce(line, frame.content, m.start)

                val scopes = withNames(frame.content, named(r.name, m))

                if r.isBegin then
                  captures(line, scopes, r.captures, m, stack, first, out)
                  out.produce(line, scopes, m.end)

                  val key = r.innerPatternKey.getOrElse("")
                  val end = if r.endRefers then endFor(r.endPattern.getOrElse(""), m) else r.endRegex
                  val content = withNames(scopes, named(r.contentName, m))
                  val again   = !advanced && reopens(stack, key, pos)

                  stack += Frame(key, end, r.endCaptures, r.endLast, scopes, content, pos, anchor, m.end == line.length)
                  anchor = m.end

                  // The same pair opened again where it already was, matching nothing: it would open for ever,
                  // so it does not, and the rest of the line is plain.
                  if again then
                    stack.remove(stack.length - 1)
                    out.produce(line, frame.content, line.length)
                    running = false
                else
                  captures(line, scopes, r.captures, m, stack, first, out)
                  out.produce(line, scopes, m.end)

                  // A rule that matched nothing closes the open pair, and the rest of the line takes the scopes
                  // left.
                  if !advanced then
                    if stack.length > 1 then stack.remove(stack.length - 1)

                    out.produce(line, stack.last.content, line.length)
                    running = false

          if running && advanced then
            pos = m.end
            first = false

  // What happens next at `pos`: the open pair's `end` (`None`) or the rule that matched, and the match.
  private def next(
      frame: Frame,
      line: String,
      pos: Int,
      anchor: Int,
      first: Boolean,
  ): Option[(Option[ResolvedPattern], OnigMatch)] =
    val em = frame.end.flatMap(searchFrom(_, line, pos, anchor, line.length))

    // The rules are searched no further than the `end` could still lose to one: a rule that begins where the
    // `end` does wins only under `applyEndPatternLast`.
    val limit = em match
      case Some(e) => if frame.endLast then e.start else e.start - 1
      case None    => line.length

    val best = nextRule(rulesFor(frame.key), first, line, pos, anchor, limit)

    (em, best) match
      case (Some(e), Some((r, bm))) =>
        val endWins = if frame.endLast then e.start < bm.start else e.start <= bm.start
        if endWins then Some((None, e)) else Some((Some(r), bm))
      case (Some(e), None)       => Some((None, e))
      case (None, Some((r, bm))) => Some((Some(r), bm))
      case (None, None)          => None

  // The first match of `re` at or after `from`, no later than `limit`.
  private def searchFrom(re: Regex, line: String, from: Int, anchor: Int, limit: Int): Option[OnigMatch] =
    var p                        = from
    var found: Option[OnigMatch] = None
    while found.isEmpty && p <= limit do
      found = re.matchAt(line, p, anchor)
      if found.isEmpty then p += (if p < line.length then Character.charCount(line.codePointAt(p)) else 1)
    found

  // The earliest match of any rule in force at or after `pos` (up to `limit`), the lower index winning a tie.
  private def nextRule(
      rules: Array[ResolvedPattern],
      first: Boolean,
      line: String,
      pos: Int,
      anchor: Int,
      limit: Int,
  ): Option[(ResolvedPattern, OnigMatch)] =
    var p = pos
    while p <= limit do
      var i = 0
      while i < rules.length do
        val r = rules(i)
        (if first then r.regex else r.regexNotFirst).matchAt(line, p, anchor) match
          case Some(m) => return Some((r, m))
          case None    => ()
        i += 1
      p += (if p < line.length then Character.charCount(line.codePointAt(p)) else 1)
    None

  // The tokens of a match's named groups, vscode-textmate's `handleCaptures`: each group's text takes its scope over
  // the scopes of the groups around it, and a group with `patterns` has its text tokenized again with those rules.
  private def captures(
      line: String,
      base: List[String],
      caps: Option[Map[String, CaptureEntry]],
      m: OnigMatch,
      stack: mutable.ArrayBuffer[Frame],
      first: Boolean,
      out: Producer,
  ): Unit =
    caps match
      case Some(cs) if cs.nonEmpty =>
        // The groups still open: their scopes and where they end.
        val open = mutable.ArrayBuffer[(List[String], Int)]()
        var i    = 0
        var stop = false

        while i <= m.groupCount && !stop do
          for
            c    <- captureFor(cs, i)
            (gs, ge) <- m.groupRange(i)
            if ge != gs
          do
            if gs > m.end then stop = true
            else
              while open.nonEmpty && open.last._2 <= gs do
                val top = open.remove(open.length - 1)
                out.produce(line, top._1, top._2)

              val around = if open.nonEmpty then open.last._1 else base

              out.produce(line, around, gs)

              if c.patterns.isDefined then
                val scopes  = withNames(base, named(c.name, m))
                val content = withNames(scopes, named(c.contentName, m))
                val inner   = stack.clone()
                inner += Frame(captureKey(c), None, None, false, scopes, content, gs, NoPos, false)
                run(line.substring(0, ge), inner, gs, NoPos, first && gs == 0, out)
              else
                named(c.name, m).foreach(n => open += ((withNames(around, Some(n)), ge)))
          i += 1

        while open.nonEmpty do
          val top = open.remove(open.length - 1)
          out.produce(line, top._1, top._2)

      case _ => ()

  // The `end` of a rule whose `end` names its `begin` match's groups: each `\N` replaced by the text group N took,
  // escaped, as vscode-textmate does. Compiled once per distinct result.
  private def endFor(src: String, m: OnigMatch): Option[Regex] =
    val resolved = resolveBackReferences(src, m)
    endsCache.getOrElseUpdate(resolved, compileRegex(resolved))

object Tokenizer:

  // The key a capture's own `patterns` are filed under.
  private[highlighter] def captureKey(c: CaptureEntry): String = s"capture_${System.identityHashCode(c)}"

  private def isDigit(c: Char): Boolean = c >= '0' && c <= '9'

  // Whether a pattern refers to a group by number -- `\` and a digit anywhere, which is the test vscode-textmate
  // makes.
  private[highlighter] def hasBackReferences(p: String): Boolean =
    p.indices.exists(i => p(i) == '\\' && i + 1 < p.length && isDigit(p(i + 1)))

  // A pattern with every `\A` made to match nothing: `\x{FFFF}`, a noncharacter, in its place.
  private[highlighter] def withoutStartAnchor(src: String): String =
    if !src.contains("\\A") then src
    else
      val out = new StringBuilder
      var i   = 0
      while i < src.length do
        if src(i) == '\\' && i + 1 < src.length then
          if src(i + 1) == 'A' then out ++= "\\x{FFFF}" else out ++= src.substring(i, i + 2)
          i += 2
        else
          out += src(i)
          i += 1
      out.toString

  // Whether the pair `key` is already open at `pos` -- among the pairs on top of the stack that were all entered
  // there.
  private def reopens(stack: mutable.ArrayBuffer[Frame], key: String, pos: Int): Boolean =
    var i = stack.length
    while i > 0 && stack(i - 1).enter == pos do
      if stack(i - 1).key == key then return true
      i -= 1
    false

  // The capture a group number has, if any: a key of digits, as written.
  private def captureFor(cs: Map[String, CaptureEntry], n: Int): Option[CaptureEntry] =
    cs.collectFirst { case (k, v) if k.nonEmpty && k.forall(isDigit) && k.toIntOption.contains(n) => v }

  // The tokens of the first `n` characters: a token starting at or past `n` is dropped and one running past it is
  // cut there.
  private def clip(tokens: List[Token], n: Int): List[Token] =
    val out = mutable.ListBuffer[Token]()
    var at  = 0
    val it  = tokens.iterator
    while it.hasNext && at < n do
      val t = it.next()
      out += (if at + t.text.length > n then Token(t.text.substring(0, n - at), t.scopes) else t)
      at += t.text.length
    out.toList

  // A fresh list: `base`, then the scopes of `a` where present.
  private def withNames(base: List[String], a: Option[String]): List[String] =
    a match
      case Some(name) => base ++ name.split(' ').filter(_.nonEmpty)
      case None       => base

  // A name with each `$N` replaced by the text group N of `m` took, leading dots dropped, and each
  // `${N:/downcase}` or `${N:/upcase}` the same text in that case -- vscode-textmate's `replaceCaptures`. A group
  // that took no part stands as nothing; a number past the pattern's groups is left as written.
  private def named(name: Option[String], m: OnigMatch): Option[String] =
    name match
      case Some(s) if s.contains("$") =>
        val out = new StringBuilder
        var lit = 0
        var i   = 0

        while i < s.length do
          if s(i) != '$' || i + 1 >= s.length then i += 1
          else
            var j       = i + 1
            var braced  = false
            if s(j) == '{' then
              braced = true
              j += 1

            var n      = 0
            val digits = j
            while j < s.length && isDigit(s(j)) do
              n = n * 10 + (s(j) - '0')
              j += 1

            var change = ""
            var ok     = j != digits

            if ok && braced then
              val rest = s.substring(j)
              if rest.startsWith(":/downcase}") then
                change = "down"
                j += 11
              else if rest.startsWith(":/upcase}") then
                change = "up"
                j += 9
              else ok = false

            if !ok then i += 1
            else
              if n <= m.groupCount then
                var t = m.group(n).getOrElse("")
                while t.startsWith(".") do t = t.substring(1)
                if change == "down" then t = t.toLowerCase
                if change == "up" then t = t.toUpperCase
                out ++= s.substring(lit, i)
                out ++= t
                lit = j
              i = j

        out ++= s.substring(lit)
        Some(out.toString)
      case other => other

  // An `end` pattern with each `\N` replaced by the text group N of the `begin` match took -- nothing where the
  // group took no part -- with every character that means something in a pattern escaped. vscode-textmate's
  // `resolveBackReferences` and `escapeRegExpCharacters`.
  private[highlighter] def resolveBackReferences(src: String, m: OnigMatch): String =
    val out = new StringBuilder
    var i   = 0
    var lit = 0

    while i < src.length do
      if src(i) == '\\' && i + 1 < src.length && isDigit(src(i + 1)) then
        out ++= src.substring(lit, i)

        var j = i + 1
        var n = 0
        while j < src.length && isDigit(src(j)) do
          n = n * 10 + (src(j) - '0')
          j += 1

        if n <= m.groupCount then m.group(n).foreach(g => out ++= escapeRegExp(g))

        i = j
        lit = j
      else i += 1

    out ++= src.substring(lit)
    out.toString

  private def escapeRegExp(s: String): String =
    val out = new StringBuilder
    for c <- s do
      if "-\\{}*+?|^$.,[]()# ".indexOf(c) >= 0 || (c >= '\t' && c <= '\r') then out += '\\'
      out += c
    out.toString
