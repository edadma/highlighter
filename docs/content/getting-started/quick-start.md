---
title: "Quick Start"
weight: 2
---

Highlighting takes three steps: load a grammar, pick how you want the output, and pass it some
code.

## A first grammar

A TextMate grammar is JSON describing the patterns of a language. A tiny one is enough to
start — keywords, numbers, strings, and line comments:

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
```

## Build a highlighter

`Highlighter.fromJson` parses the grammar and returns `Either[String, Highlighter]` — a `Left`
carries a parse error message, so this never throws on a bad grammar:

```scala
val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
```

## Highlight some code

```scala
println(hl.highlight("val x = 42"))
// <span class="hl-keyword">val</span> x = <span class="hl-number">42</span>
```

That is the whole loop. From here:

- **[Loading Grammars](/guide/grammars/)** — use a real VS Code `.tmLanguage.json`, and the
  grammar features highlighter supports.
- **[HTML Rendering](/guide/rendering/)** — CSS-class output versus inline theme colours.
- **[Themes](/guide/themes/)** — the built-in colour schemes and writing your own.
- **[Tokens](/guide/tokens/)** — skip the HTML and render the tokens yourself.
