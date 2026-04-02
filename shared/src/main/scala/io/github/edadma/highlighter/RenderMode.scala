package io.github.edadma.highlighter

sealed trait RenderMode

case class ClassMode(prefix: String = "hl-") extends RenderMode

case class InlineMode(theme: Theme) extends RenderMode

case class Theme(
    keyword: String = "#c678dd",
    string: String = "#98c379",
    comment: String = "#5c6370",
    number: String = "#d19a66",
    `type`: String = "#e5c07b",
    function: String = "#61afef",
    variable: String = "#e06c75",
    operator: String = "#56b6c2",
    punctuation: String = "#abb2bf",
    default: String = "#abb2bf",
)

object Theme:
  val OneDark: Theme = Theme()

  val OneLight: Theme = Theme(
    keyword = "#a626a4",
    string = "#50a14f",
    comment = "#a0a1a7",
    number = "#986801",
    `type` = "#c18401",
    function = "#4078f2",
    variable = "#e45649",
    operator = "#0184bc",
    punctuation = "#383a42",
    default = "#383a42",
  )
