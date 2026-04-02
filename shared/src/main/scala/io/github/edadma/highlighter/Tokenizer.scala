package io.github.edadma.highlighter

import scala.util.matching.Regex
import scala.collection.mutable

case class ResolvedPattern(
    name: Option[String],
    regex: Regex,
    isBegin: Boolean,
    endPattern: Option[String],
    endCaptures: Option[Map[String, CaptureEntry]],
    captures: Option[Map[String, CaptureEntry]], // match captures or beginCaptures
    innerPatternKey: Option[String],
    contentName: Option[String],
    isSelfMarker: Boolean = false,
)

// Sentinel pattern for $self includes — resolved lazily at tokenization time
private[highlighter] val SelfMarker = ResolvedPattern(
  name = None, regex = "(?!)".r, isBegin = false,
  endPattern = None, endCaptures = None, captures = None,
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

  private val topPatterns: List[ResolvedPattern] = resolveAndCache("$top", grammar.patterns)

  private def preprocessRegex(pat: String): String =
    var result = pat
    for cls <- posixClasses do
      result = result.replace(cls, translatePosixClass(cls))
    result

  private def compileRegex(pat: String): Option[Regex] =
    try Some(new Regex(preprocessRegex(pat)))
    catch case _: Exception => None

  private def resolveAndCache(key: String, patterns: List[Pattern]): List[ResolvedPattern] =
    resolvedCache.get(key) match
      case Some(cached) => cached
      case None =>
        resolvedCache(key) = Nil // sentinel to break cycles
        val result = resolvePatterns(patterns, Set(key))
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
        if visited.contains(key) then Nil
        else
          resolvedCache.get(key) match
            case Some(cached) => cached
            case None =>
              repository.get(key) match
                case Some(entry) =>
                  resolvedCache(key) = Nil // sentinel
                  val newVisited = visited + key
                  val result = entry.patterns match
                    case Some(ps) => resolvePatterns(ps, newVisited)
                    case None =>
                      val pat = RepositoryEntry.toPattern(entry)
                      resolveOne(pat, newVisited)
                  resolvedCache(key) = result
                  result
                case None => Nil

      case Some(_) => Nil

      case None =>
        if p.`match`.isDefined then
          compileRegex(p.`match`.get).toList.map { r =>
            ResolvedPattern(p.name, r, isBegin = false, None, None, p.captures, None, None)
          }
        else if p.begin.isDefined then
          compileRegex(p.begin.get).toList.map { r =>
            val innerKey = s"inner_${System.identityHashCode(p)}"
            p.patterns.foreach(ps => resolveAndCache(innerKey, ps))
            ResolvedPattern(
              p.name, r, isBegin = true, p.end, p.endCaptures,
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
              if gs >= 0 && ge >= 0 then
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
            val relStart = gs - m.start
            val relEnd = ge - m.start
            if relStart > pos then
              tokens += Token(matchedText.substring(pos, relStart), effectiveScopes)
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
      val sub = line.substring(pos)

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
          val endRegex = bestPat.endPattern.flatMap(compileRegex)
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
