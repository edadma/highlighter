---
title: "HTML Rendering"
weight: 2
---

`highlight(code)` returns an HTML string with one `<span>` per highlighted run. How those spans
are styled depends on the **render mode** you pass when building the highlighter. Text is
HTML-escaped, and adjacent runs with the same scope are merged into one span.

## Class mode

`ClassMode(prefix)` emits `<span class="prefix-category">`, where the category is one of nine
names. You supply the colours in your own stylesheet:

```scala
val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
hl.highlight("val x = 42")
// <span class="hl-keyword">val</span> x = <span class="hl-number">42</span>
```

Change the prefix to match your CSS:

```scala
ClassMode("hl-")    // <span class="hl-keyword">
ClassMode("code-")  // <span class="code-keyword">
```

The nine categories are: `keyword`, `string`, `comment`, `number`, `type`, `function`,
`variable`, `operator`, `punctuation`.

## Inline mode

`InlineMode(theme)` emits `<span style="color:#hex">` straight from a theme, so the output is
self-contained — no stylesheet required:

```scala
val Right(hl) = Highlighter.fromJson(grammar, InlineMode(Theme.OneDark)): @unchecked
hl.highlight("val x = 42")
// <span style="color:#c678dd">val</span> x = <span style="color:#d19a66">42</span>
```

This very page is built that way: the plain fenced ` ```scala ` blocks here are highlighted at
build time by the same library, through the site generator's `codeHighlighter` hook:

```scala
// quicksort
def sort(xs: List[Int]): List[Int] = xs match
  case Nil => Nil
  case pivot :: rest =>
    val (less, more) = rest.partition(_ < pivot)
    sort(less) ++ (pivot :: sort(more))

val nums = List(5, 2, 8, 1, 9)
println(sort(nums))  // List(1, 2, 5, 8, 9)
```

See [Themes](/guide/themes/) for the built-in schemes and how to define your own.

## Lines

`highlight` joins lines with `\n`; each source line is highlighted independently and a
multi-line `begin`/`end` span carries its scope across the break. If you are rendering to
something other than HTML — and want the line structure handed to you — use the
[token API](/guide/tokens/) instead.
