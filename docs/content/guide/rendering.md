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

Dropped into a `<pre>`, that output renders as real highlighted code — no stylesheet involved:

<pre style="background:#282c34;color:#abb2bf;padding:1rem 1.25rem;border-radius:8px;overflow-x:auto;font-size:.8rem;line-height:1.55;margin:1rem 0"><code><span style="color:#5c6370">// quicksort</span>
<span style="color:#c678dd">def</span> sort<span style="color:#abb2bf">(</span>xs<span style="color:#abb2bf">:</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]):</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]</span> <span style="color:#c678dd">=</span> xs <span style="color:#c678dd">match</span>
  <span style="color:#c678dd">case</span> <span style="color:#e5c07b">Nil</span> <span style="color:#c678dd">=&gt;</span> <span style="color:#e5c07b">Nil</span>
  <span style="color:#c678dd">case</span> pivot <span style="color:#c678dd">::</span> rest <span style="color:#c678dd">=&gt;</span>
    <span style="color:#c678dd">val</span> <span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">,</span> more<span style="color:#abb2bf">)</span> <span style="color:#c678dd">=</span> rest<span style="color:#abb2bf">.</span>partition<span style="color:#abb2bf">(</span>_ <span style="color:#c678dd">&lt;</span> pivot<span style="color:#abb2bf">)</span>
    sort<span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">)</span> <span style="color:#c678dd">++</span> <span style="color:#abb2bf">(</span>pivot <span style="color:#c678dd">::</span> sort<span style="color:#abb2bf">(</span>more<span style="color:#abb2bf">))</span>

<span style="color:#c678dd">val</span> nums <span style="color:#c678dd">=</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">(</span><span style="color:#d19a66">5</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">2</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">8</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">1</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">9</span><span style="color:#abb2bf">)</span>
println<span style="color:#abb2bf">(</span>sort<span style="color:#abb2bf">(</span>nums<span style="color:#abb2bf">))</span>  <span style="color:#5c6370">// List(1, 2, 5, 8, 9)</span></code></pre>

See [Themes](/guide/themes/) for the built-in schemes and how to define your own.

## Lines

`highlight` joins lines with `\n`; each source line is highlighted independently and a
multi-line `begin`/`end` span carries its scope across the break. If you are rendering to
something other than HTML — and want the line structure handed to you — use the
[token API](/guide/tokens/) instead.
