package io.github.edadma.highlighter

import zio.json.*

class Highlighter(grammar: Grammar, mode: RenderMode = ClassMode()):
  private val tokenizer = new Tokenizer(grammar)

  /** Diagnostic warnings collected while resolving the grammar's patterns —
    * every regex that failed to compile (typically because it uses
    * Oniguruma syntax that java.util.regex doesn't accept) lands here.
    * Empty list means "every pattern compiled cleanly".
    *
    * Tools should print these once (see `Highlighter.fromJson` for the
    * convenience constructor that does that automatically). The buffer
    * is captured at construction time so reading this is free.
    */
  def loadWarnings: List[String] = tokenizer.loadWarnings

  def highlight(code: String): String =
    val tokenLines = tokenizer.tokenize(code)
    tokenLines
      .map { tokens =>
        mergeTokens(tokens).map(renderToken).mkString
      }
      .mkString("\n")

  /** Tokenize `code` into one list of tokens per source line, with adjacent same-scope tokens merged — the
    * structured equivalent of [[highlight]] for callers that render the tokens themselves (a PDF or terminal
    * back end, say) rather than HTML. The line grouping is preserved: `tokens(code).length` is the number of
    * lines, and concatenating each line's token texts reproduces that line exactly. */
  def tokens(code: String): List[List[Token]] =
    tokenizer.tokenize(code).map(mergeTokens)

  /** Collapse a TextMate scope to one of the rendering categories — `keyword`, `string`, `comment`, `number`,
    * `type`, `function`, `variable`, `operator`, `punctuation` — or `""` when the scope carries no colour of its
    * own (plain source text). The HTML back ends use this to pick a CSS class or theme colour; a token-consuming
    * caller uses it the same way against its own palette. A token's category comes from its whole scope stack:
    * see [[categoryOf]]. */
  def category(scope: String): String = scopeCategory(scope)

  /** The rendering category of a token, read from its whole scope stack the way a TextMate theme reads it. The
    * innermost scope that has a category wins, so text inside a string with an uncoloured `meta.…` scope of its
    * own is still a string. The exception is a `punctuation.definition.…` scope — the quotes of a string, the
    * marker of a comment — which belongs to the construct it delimits: it takes the category of the nearest
    * enclosing scope that has one, and is `punctuation` only when none does. */
  def categoryOf(token: Token): String =
    val categorised = token.scopes.reverse.map(s => (s, scopeCategory(s))).filter(_._2.nonEmpty)
    categorised.find(!_._1.startsWith("punctuation.definition.")) match
      case Some((_, category))          => category
      case None if categorised.nonEmpty => "punctuation"
      case None                         => ""

  private def mergeTokens(tokens: List[Token]): List[Token] =
    tokens.foldLeft(List.empty[Token]) { (acc, tok) =>
      acc match
        case prev :: rest if prev.scopes == tok.scopes =>
          Token(prev.text + tok.text, prev.scopes) :: rest
        case _ => tok :: acc
    }.reverse

  private def renderToken(token: Token): String =
    if token.text.isEmpty then return ""

    val escaped  = escapeHtml(token.text)
    val category = categoryOf(token)

    mode match
      case ClassMode(prefix) =>
        if category.nonEmpty then s"""<span class="$prefix$category">$escaped</span>"""
        else escaped

      case InlineMode(theme) =>
        val color = categoryColor(category, theme)
        if color.nonEmpty then s"""<span style="color:$color">$escaped</span>"""
        else escaped

  private def escapeHtml(s: String): String =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")

  private def categoryColor(category: String, theme: Theme): String =
    category match
      case "keyword"     => theme.keyword
      case "string"      => theme.string
      case "comment"     => theme.comment
      case "number"      => theme.number
      case "type"        => theme.`type`
      case "function"    => theme.function
      case "variable"    => theme.variable
      case "operator"    => theme.operator
      case "punctuation" => theme.punctuation
      case ""            => ""
      case _             => theme.default

  private def scopeCategory(scope: String): String =
    val parts = scope.split('.')
    if parts.isEmpty then ""
    else
      parts.head match
        case "keyword" | "storage"                     => "keyword"
        case "string"                                  => "string"
        case "comment"                                 => "comment"
        case "constant" if scope.contains("numeric")   => "number"
        case "constant"                                => "variable"
        case "entity" if scope.contains("name.type")   => "type"
        case "entity" if scope.contains("name.function") => "function"
        case "entity"                                  => "function"
        case "variable"                                => "variable"
        case "support" if scope.contains("type")       => "type"
        case "support" if scope.contains("function")   => "function"
        case "punctuation"                             => "punctuation"
        case "meta"                                    => ""
        case "source"                                  => ""
        case _                                         => ""


object Highlighter:
  def fromJson(json: String, mode: RenderMode = ClassMode()): Either[String, Highlighter] =
    json.fromJson[Grammar].map(g => new Highlighter(g, mode))
