---
title: "Loading Grammars"
weight: 1
---

highlighter reads **TextMate grammars** — the `.tmLanguage.json` files that VS Code and TextMate
use to describe a language. Any VS Code-compatible grammar works unchanged.

## Loading a VS Code grammar

Read the grammar file as a string and hand it to `fromJson`:

```scala
val grammarJson = scala.io.Source.fromFile("JavaScript.tmLanguage.json").mkString
val Right(hl)   = Highlighter.fromJson(grammarJson, ClassMode("code-")): @unchecked

println(hl.highlight("const x = 42;"))
```

On Scala.js or Scala Native, load the grammar however that platform reads text — a bundled
resource, an HTTP fetch, a file — and pass the resulting string in the same way.

## Supported grammar features

The grammar engine is a regex state machine, not a flat list of patterns. It supports the
features real-world grammars rely on:

- `match` patterns with a `name` and numbered `captures`.
- `begin`/`end` patterns with `beginCaptures`, `endCaptures`, and nested `patterns`, so a
  construct can span lines (a block comment, a multi-line string).
- `contentName` to scope the region *between* a begin and end separately from the delimiters.
- A `repository` of named rule sets, referenced with `include`.
- `include` references: `#ruleName` for a repository rule and `$self` for the whole grammar —
  recursive and cyclic includes are handled with cycle detection.
- POSIX character classes (`[:alpha:]`, `[:digit:]`, …), translated per platform.

## When a pattern can't compile

A grammar occasionally uses an Oniguruma regex feature that doesn't translate. Rather than fail
the whole grammar, highlighter skips the offending pattern and records a diagnostic:

```scala
val Right(hl) = Highlighter.fromJson(grammarJson): @unchecked
hl.loadWarnings.foreach(println)   // empty when every pattern compiled cleanly
```

The buffer is captured when the highlighter is built, so reading it is free. Print it once at
load time to see whether any patterns were dropped.
