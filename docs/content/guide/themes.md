---
title: "Themes"
weight: 3
---

A `Theme` maps each of the nine highlight categories to a colour. Themes are used by
`InlineMode` to emit inline `style` colours, and you can read a theme yourself when rendering
[tokens](/guide/tokens/).

## Built-in themes

Three presets ship with the library:

```scala
InlineMode(Theme.OneDark)   // dark
InlineMode(Theme.OneLight)  // light
InlineMode(Theme.Monokai)   // dark
```

## Custom themes

`Theme` is a plain case class with a default colour per category, so override only what you
want:

```scala
val mine = Theme(
  keyword     = "#ff0000",
  string      = "#00aa00",
  comment     = "#888888",
  number      = "#d19a66",
  `type`      = "#e5c07b",
  function    = "#61afef",
  variable    = "#e06c75",
  operator    = "#56b6c2",
  punctuation = "#abb2bf",
  default     = "#abb2bf",   // anything with no category of its own
)

val Right(hl) = Highlighter.fromJson(grammar, InlineMode(mine)): @unchecked
```

`keyword` covers control words and storage modifiers; `default` is used for text that carries a
scope highlighter doesn't map to a category. (`type` is a Scala keyword, so it is written
`` `type` `` in source.)
