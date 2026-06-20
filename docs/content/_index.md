---
title: highlighter
splash: true
heroTitle: A TextMate-grammar syntax highlighter for
heroHighlight: Scala
summary: Tokenize and highlight source code with the same VS Code-compatible TextMate grammars your editor uses. Render to HTML — CSS classes or inline theme colours — or take the tokens directly and draw them your own way. Cross-built for the JVM, Scala.js, and Scala Native.
getStarted: true
---

## Why highlighter

highlighter turns source code into highlighted output using **TextMate grammars** — the same
`.tmLanguage.json` files that drive VS Code. Point it at a grammar, hand it a snippet, and it
gives you back highlighted HTML, or the raw tokens to render however you like.

```scala
import io.github.edadma.highlighter.*

val Right(hl) = Highlighter.fromJson(grammarJson, InlineMode(Theme.OneDark)): @unchecked
println(hl.highlight("val x = 42"))
// <span style="color:#c678dd">val</span> x = <span style="color:#d19a66">42</span>
```

The grammar engine is a full regex state machine — begin/end spans, captures, repository
includes, `$self` references, cycle detection — running on a pure-Scala port of Oniguruma, so
the **same grammars work identically on every platform**, with no native dependency.

## What's here

- **[Getting Started](/getting-started/)** — add the dependency and highlight your first snippet.
- **[Guide](/guide/)** — loading grammars, the HTML render modes, themes, and consuming tokens directly.

## What it does

- **TextMate grammar engine** — `match` and `begin`/`end` patterns, `captures`,
  `contentName`, `repository` rule sets, `#rule` / `$self` includes, with cycle detection.
- **Two HTML render modes** — CSS classes with a configurable prefix, or inline `style`
  colours from a theme.
- **Built-in themes** — OneDark, OneLight, and Monokai, or your own colour scheme.
- **Tokens, not just HTML** — take `List[List[Token]]` (one list per source line) and render
  to a PDF, a terminal, or a GUI against your own palette.
- **Three platforms** — the JVM, Scala.js, and Scala Native, all from one pure-Scala
  codebase.
