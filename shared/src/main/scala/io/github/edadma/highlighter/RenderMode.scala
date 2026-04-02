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

  val Monokai: Theme = Theme(
    keyword = "#f92672",
    string = "#e6db74",
    comment = "#75715e",
    number = "#ae81ff",
    `type` = "#a6e22e",
    function = "#a6e22e",
    variable = "#f8f8f2",
    operator = "#f92672",
    punctuation = "#f8f8f2",
    default = "#f8f8f2",
  )

  val Dracula: Theme = Theme(
    keyword = "#ff79c6",
    string = "#f1fa8c",
    comment = "#6272a4",
    number = "#bd93f9",
    `type` = "#8be9fd",
    function = "#50fa7b",
    variable = "#f8f8f2",
    operator = "#ff79c6",
    punctuation = "#f8f8f2",
    default = "#f8f8f2",
  )

  val SolarizedDark: Theme = Theme(
    keyword = "#859900",
    string = "#2aa198",
    comment = "#586e75",
    number = "#d33682",
    `type` = "#b58900",
    function = "#268bd2",
    variable = "#cb4b16",
    operator = "#859900",
    punctuation = "#839496",
    default = "#839496",
  )

  val SolarizedLight: Theme = Theme(
    keyword = "#859900",
    string = "#2aa198",
    comment = "#93a1a1",
    number = "#d33682",
    `type` = "#b58900",
    function = "#268bd2",
    variable = "#cb4b16",
    operator = "#859900",
    punctuation = "#657b83",
    default = "#657b83",
  )

  val GitHubDark: Theme = Theme(
    keyword = "#ff7b72",
    string = "#a5d6ff",
    comment = "#8b949e",
    number = "#79c0ff",
    `type` = "#ffa657",
    function = "#d2a8ff",
    variable = "#ffa657",
    operator = "#ff7b72",
    punctuation = "#c9d1d9",
    default = "#c9d1d9",
  )

  val GitHubLight: Theme = Theme(
    keyword = "#cf222e",
    string = "#0a3069",
    comment = "#6e7781",
    number = "#0550ae",
    `type` = "#953800",
    function = "#8250df",
    variable = "#953800",
    operator = "#cf222e",
    punctuation = "#24292f",
    default = "#24292f",
  )

  val Nord: Theme = Theme(
    keyword = "#81a1c1",
    string = "#a3be8c",
    comment = "#616e88",
    number = "#b48ead",
    `type` = "#8fbcbb",
    function = "#88c0d0",
    variable = "#d8dee9",
    operator = "#81a1c1",
    punctuation = "#eceff4",
    default = "#d8dee9",
  )

  val GruvboxDark: Theme = Theme(
    keyword = "#fb4934",
    string = "#b8bb26",
    comment = "#928374",
    number = "#d3869b",
    `type` = "#fabd2f",
    function = "#8ec07c",
    variable = "#83a598",
    operator = "#fe8019",
    punctuation = "#ebdbb2",
    default = "#ebdbb2",
  )

  val GruvboxLight: Theme = Theme(
    keyword = "#9d0006",
    string = "#79740e",
    comment = "#928374",
    number = "#8f3f71",
    `type` = "#b57614",
    function = "#427b58",
    variable = "#076678",
    operator = "#af3a03",
    punctuation = "#3c3836",
    default = "#3c3836",
  )

  val TokyoNight: Theme = Theme(
    keyword = "#bb9af7",
    string = "#9ece6a",
    comment = "#565f89",
    number = "#ff9e64",
    `type` = "#2ac3de",
    function = "#7aa2f7",
    variable = "#c0caf5",
    operator = "#89ddff",
    punctuation = "#a9b1d6",
    default = "#a9b1d6",
  )

  val CatppuccinMocha: Theme = Theme(
    keyword = "#cba6f7",
    string = "#a6e3a1",
    comment = "#6c7086",
    number = "#fab387",
    `type` = "#f9e2af",
    function = "#89b4fa",
    variable = "#f38ba8",
    operator = "#89dceb",
    punctuation = "#cdd6f4",
    default = "#cdd6f4",
  )
