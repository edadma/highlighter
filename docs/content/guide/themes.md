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

The same Scala snippet under each — and every one of these blocks was produced by highlighter
itself, in inline mode:

**OneDark**

<pre style="background:#282c34;color:#abb2bf;padding:1rem 1.25rem;border-radius:8px;overflow-x:auto;font-size:.8rem;line-height:1.55;margin:1rem 0"><code><span style="color:#5c6370">// quicksort</span>
<span style="color:#c678dd">def</span> sort<span style="color:#abb2bf">(</span>xs<span style="color:#abb2bf">:</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]):</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]</span> <span style="color:#c678dd">=</span> xs <span style="color:#c678dd">match</span>
  <span style="color:#c678dd">case</span> <span style="color:#e5c07b">Nil</span> <span style="color:#c678dd">=&gt;</span> <span style="color:#e5c07b">Nil</span>
  <span style="color:#c678dd">case</span> pivot <span style="color:#c678dd">::</span> rest <span style="color:#c678dd">=&gt;</span>
    <span style="color:#c678dd">val</span> <span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">,</span> more<span style="color:#abb2bf">)</span> <span style="color:#c678dd">=</span> rest<span style="color:#abb2bf">.</span>partition<span style="color:#abb2bf">(</span>_ <span style="color:#c678dd">&lt;</span> pivot<span style="color:#abb2bf">)</span>
    sort<span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">)</span> <span style="color:#c678dd">++</span> <span style="color:#abb2bf">(</span>pivot <span style="color:#c678dd">::</span> sort<span style="color:#abb2bf">(</span>more<span style="color:#abb2bf">))</span>

<span style="color:#c678dd">val</span> nums <span style="color:#c678dd">=</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">(</span><span style="color:#d19a66">5</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">2</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">8</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">1</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">9</span><span style="color:#abb2bf">)</span>
println<span style="color:#abb2bf">(</span>sort<span style="color:#abb2bf">(</span>nums<span style="color:#abb2bf">))</span>  <span style="color:#5c6370">// List(1, 2, 5, 8, 9)</span></code></pre>

**OneLight**

<pre style="background:#fafafa;color:#383a42;padding:1rem 1.25rem;border-radius:8px;overflow-x:auto;font-size:.8rem;line-height:1.55;margin:1rem 0"><code><span style="color:#a0a1a7">// quicksort</span>
<span style="color:#a626a4">def</span> sort<span style="color:#383a42">(</span>xs<span style="color:#383a42">:</span> <span style="color:#c18401">List</span><span style="color:#383a42">[</span><span style="color:#c18401">Int</span><span style="color:#383a42">]):</span> <span style="color:#c18401">List</span><span style="color:#383a42">[</span><span style="color:#c18401">Int</span><span style="color:#383a42">]</span> <span style="color:#a626a4">=</span> xs <span style="color:#a626a4">match</span>
  <span style="color:#a626a4">case</span> <span style="color:#c18401">Nil</span> <span style="color:#a626a4">=&gt;</span> <span style="color:#c18401">Nil</span>
  <span style="color:#a626a4">case</span> pivot <span style="color:#a626a4">::</span> rest <span style="color:#a626a4">=&gt;</span>
    <span style="color:#a626a4">val</span> <span style="color:#383a42">(</span>less<span style="color:#383a42">,</span> more<span style="color:#383a42">)</span> <span style="color:#a626a4">=</span> rest<span style="color:#383a42">.</span>partition<span style="color:#383a42">(</span>_ <span style="color:#a626a4">&lt;</span> pivot<span style="color:#383a42">)</span>
    sort<span style="color:#383a42">(</span>less<span style="color:#383a42">)</span> <span style="color:#a626a4">++</span> <span style="color:#383a42">(</span>pivot <span style="color:#a626a4">::</span> sort<span style="color:#383a42">(</span>more<span style="color:#383a42">))</span>

<span style="color:#a626a4">val</span> nums <span style="color:#a626a4">=</span> <span style="color:#c18401">List</span><span style="color:#383a42">(</span><span style="color:#986801">5</span><span style="color:#383a42">,</span> <span style="color:#986801">2</span><span style="color:#383a42">,</span> <span style="color:#986801">8</span><span style="color:#383a42">,</span> <span style="color:#986801">1</span><span style="color:#383a42">,</span> <span style="color:#986801">9</span><span style="color:#383a42">)</span>
println<span style="color:#383a42">(</span>sort<span style="color:#383a42">(</span>nums<span style="color:#383a42">))</span>  <span style="color:#a0a1a7">// List(1, 2, 5, 8, 9)</span></code></pre>

**Monokai**

<pre style="background:#272822;color:#f8f8f2;padding:1rem 1.25rem;border-radius:8px;overflow-x:auto;font-size:.8rem;line-height:1.55;margin:1rem 0"><code><span style="color:#75715e">// quicksort</span>
<span style="color:#f92672">def</span> sort<span style="color:#f8f8f2">(</span>xs<span style="color:#f8f8f2">:</span> <span style="color:#a6e22e">List</span><span style="color:#f8f8f2">[</span><span style="color:#a6e22e">Int</span><span style="color:#f8f8f2">]):</span> <span style="color:#a6e22e">List</span><span style="color:#f8f8f2">[</span><span style="color:#a6e22e">Int</span><span style="color:#f8f8f2">]</span> <span style="color:#f92672">=</span> xs <span style="color:#f92672">match</span>
  <span style="color:#f92672">case</span> <span style="color:#a6e22e">Nil</span> <span style="color:#f92672">=&gt;</span> <span style="color:#a6e22e">Nil</span>
  <span style="color:#f92672">case</span> pivot <span style="color:#f92672">::</span> rest <span style="color:#f92672">=&gt;</span>
    <span style="color:#f92672">val</span> <span style="color:#f8f8f2">(</span>less<span style="color:#f8f8f2">,</span> more<span style="color:#f8f8f2">)</span> <span style="color:#f92672">=</span> rest<span style="color:#f8f8f2">.</span>partition<span style="color:#f8f8f2">(</span>_ <span style="color:#f92672">&lt;</span> pivot<span style="color:#f8f8f2">)</span>
    sort<span style="color:#f8f8f2">(</span>less<span style="color:#f8f8f2">)</span> <span style="color:#f92672">++</span> <span style="color:#f8f8f2">(</span>pivot <span style="color:#f92672">::</span> sort<span style="color:#f8f8f2">(</span>more<span style="color:#f8f8f2">))</span>

<span style="color:#f92672">val</span> nums <span style="color:#f92672">=</span> <span style="color:#a6e22e">List</span><span style="color:#f8f8f2">(</span><span style="color:#ae81ff">5</span><span style="color:#f8f8f2">,</span> <span style="color:#ae81ff">2</span><span style="color:#f8f8f2">,</span> <span style="color:#ae81ff">8</span><span style="color:#f8f8f2">,</span> <span style="color:#ae81ff">1</span><span style="color:#f8f8f2">,</span> <span style="color:#ae81ff">9</span><span style="color:#f8f8f2">)</span>
println<span style="color:#f8f8f2">(</span>sort<span style="color:#f8f8f2">(</span>nums<span style="color:#f8f8f2">))</span>  <span style="color:#75715e">// List(1, 2, 5, 8, 9)</span></code></pre>

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
