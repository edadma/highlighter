# highlighter

![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/highlighter_sjs1_3)
[![Last Commit](https://img.shields.io/github/last-commit/edadma/highlighter)](https://github.com/edadma/highlighter/commits)
![GitHub](https://img.shields.io/github/license/edadma/highlighter)
![Scala Version](https://img.shields.io/badge/Scala-3.8.4-blue.svg)
![ScalaJS Version](https://img.shields.io/badge/Scala.js-1.21.0-blue.svg)
![Scala Native Version](https://img.shields.io/badge/Scala_Native-0.5.12-blue.svg)

A cross-platform Scala syntax highlighter that uses VS Code-compatible TextMate grammar JSON files to tokenize code and render highlighted HTML — or to hand you the tokens directly to render your own way.

## Features

- **TextMate grammar engine** — regex state machine with begin/end patterns, captures, repository includes, `$self` references, and cycle detection
- **Two rendering modes** — CSS classes with configurable prefix, or inline styles with theme colors
- **Cross-platform** — compiles to JVM, JavaScript (Scala.js), and Native (Scala Native)
- **Built-in themes** — OneDark and OneLight presets, or pass your own color scheme

## Usage

### Add dependency

```scala
libraryDependencies += "io.github.edadma" %%% "highlighter" % "0.0.10"
```

### Highlight with a grammar

```scala
import io.github.edadma.highlighter.*

val grammar = """{
  "scopeName": "source.example",
  "patterns": [
    { "match": "\\b(val|def|if|else)\\b", "name": "keyword.control" },
    { "match": "\\b\\d+\\b", "name": "constant.numeric" },
    { "begin": "\"", "end": "\"", "name": "string.quoted.double" },
    { "match": "//.*$", "name": "comment.line" }
  ]
}"""

// CSS class mode (default)
val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
println(hl.highlight("val x = 42"))
// <span class="hl-keyword">val</span> x = <span class="hl-number">42</span>

// Inline style mode
val Right(hl2) = Highlighter.fromJson(grammar, InlineMode(Theme.OneDark)): @unchecked
println(hl2.highlight("val x = 42"))
// <span style="color:#c678dd">val</span> x = <span style="color:#d19a66">42</span>
```

### Load a VS Code grammar

Any VS Code-compatible `.tmLanguage.json` file works:

```scala
val grammarJson = scala.io.Source.fromFile("JavaScript.tmLanguage.json").mkString
val Right(hl) = Highlighter.fromJson(grammarJson, ClassMode("code-")): @unchecked
println(hl.highlight("const x = 42;"))
```

### Tokens instead of HTML

When you render to something other than a web page — a PDF, a terminal, a GUI — take the tokens directly instead of parsing the HTML back apart. `tokens` returns one merged token list **per source line**, and `category` / `categoryOf` map a token to the same nine categories the HTML back ends use, against your own palette:

```scala
val Right(hl) = Highlighter.fromJson(grammar): @unchecked

for line <- hl.tokens("val x = 42") do
  for tok <- line do
    val colour = hl.categoryOf(tok) match  // keyword | string | comment | number | type | ...
      case "keyword" => myKeywordColour
      case "number"  => myNumberColour
      case _         => myDefaultColour
    draw(tok.text, colour)
```

Each `Token` is `(text, scopes)`; its category comes from the innermost scope. The line grouping is preserved, so concatenating a line's token texts reproduces that line exactly.

### Rendering modes

**`ClassMode(prefix)`** emits `<span class="prefix-category">` where category is one of: `keyword`, `string`, `comment`, `number`, `type`, `function`, `variable`, `operator`, `punctuation`.

```scala
ClassMode("hl-")    // <span class="hl-keyword">
ClassMode("code-")  // <span class="code-keyword">
```

**`InlineMode(theme)`** emits `<span style="color:#hex">` using a color theme.

```scala
InlineMode(Theme.OneDark)   // dark theme
InlineMode(Theme.OneLight)  // light theme
InlineMode(Theme(           // custom colors
  keyword = "#ff0000",
  string = "#00ff00",
  comment = "#888888",
  // ...
))
```

### Grammar features supported

- `match` patterns with `name` and `captures`
- `begin`/`end` patterns with `beginCaptures`, `endCaptures`, and nested `patterns`
- `contentName` for scoping region content
- `repository` with named rule sets
- `include` references: `#ruleName`, `$self`
- Recursive/cyclic includes (with cycle detection)
- POSIX character classes (`[:alpha:]`, `[:digit:]`, etc.) translated per platform

## License

ISC
