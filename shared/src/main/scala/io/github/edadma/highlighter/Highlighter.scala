package io.github.edadma.highlighter

import zio.json.*

class Highlighter(grammar: Grammar, mode: RenderMode = ClassMode()):
  private val tokenizer = new Tokenizer(grammar)

  def highlight(code: String): String =
    val tokenLines = tokenizer.tokenize(code)
    tokenLines
      .map { tokens =>
        mergeTokens(tokens).map(renderToken).mkString
      }
      .mkString("\n")

  private def mergeTokens(tokens: List[Token]): List[Token] =
    tokens.foldLeft(List.empty[Token]) { (acc, tok) =>
      acc match
        case prev :: rest if prev.scopes == tok.scopes =>
          Token(prev.text + tok.text, prev.scopes) :: rest
        case _ => tok :: acc
    }.reverse

  private def renderToken(token: Token): String =
    if token.text.isEmpty then return ""

    val escaped = escapeHtml(token.text)
    val scope = token.scopes.lastOption.getOrElse("")

    mode match
      case ClassMode(prefix) =>
        val cls = scopeToClass(scope, prefix)
        if cls.nonEmpty then s"""<span class="$cls">$escaped</span>"""
        else escaped

      case InlineMode(theme) =>
        val color = scopeToColor(scope, theme)
        if color.nonEmpty then s"""<span style="color:$color">$escaped</span>"""
        else escaped

  private def escapeHtml(s: String): String =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")

  private def scopeToClass(scope: String, prefix: String): String =
    if scope.isEmpty then ""
    else
      val category = scopeCategory(scope)
      if category.nonEmpty then s"$prefix$category" else ""

  private def scopeToColor(scope: String, theme: Theme): String =
    scopeCategory(scope) match
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
