---
title: "Tokens"
weight: 4
---

When you render to something other than a web page — a PDF, a terminal, a GUI — take the tokens
directly instead of producing HTML and parsing it back apart. `tokens` returns the same
information `highlight` uses, structured rather than serialized.

## Tokenizing

`tokens(code)` returns **one list of tokens per source line**, with adjacent same-scope tokens
merged:

```scala
val Right(hl) = Highlighter.fromJson(grammar): @unchecked

for line <- hl.tokens("val x = 42\n// note") do
  for tok <- line do
    render(tok.text, hl.categoryOf(tok))
```

Each `Token` is:

```scala
case class Token(text: String, scopes: List[String])
```

The line grouping is preserved: `tokens(code).length` is the number of lines, and
concatenating a line's token texts reproduces that line exactly — so you control line breaks and
indentation yourself.

## Categories and colours

A token's effective scope is its innermost (last) one. `categoryOf` collapses it to one of the
nine rendering categories — the same mapping the HTML back ends use — so you can drive your own
palette:

```scala
def colour(tok: Token): MyColour =
  hl.categoryOf(tok) match           // keyword | string | comment | number | type |
    case "keyword" => myKeyword      // function | variable | operator | punctuation | ""
    case "string"  => myString
    case "comment" => myComment
    case "number"  => myNumber
    case _         => myDefault
```

An empty category (`""`) means the token carries no colour of its own — plain source text.
`category(scope)` is the same mapping applied to a raw scope string, if you have one in hand.

## Pairing with a theme

The built-in [themes](/guide/themes/) are just category-to-hex maps, so you can reuse one as
your palette instead of hand-rolling colours:

```scala
val theme = Theme.OneDark
def hex(tok: Token): String =
  hl.categoryOf(tok) match
    case "keyword" => theme.keyword
    case "string"  => theme.string
    case "number"  => theme.number
    case _         => theme.default
```
