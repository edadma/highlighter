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

A Scala snippet highlighted with the OneDark theme — and this block, like every highlighted one
on this site, was produced by highlighter itself:

<pre style="background:#282c34;color:#abb2bf;padding:1rem 1.25rem;border-radius:8px;overflow-x:auto;font-size:.8rem;line-height:1.55;margin:1rem 0"><code><span style="color:#5c6370">// quicksort</span>
<span style="color:#c678dd">def</span> sort<span style="color:#abb2bf">(</span>xs<span style="color:#abb2bf">:</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]):</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">[</span><span style="color:#e5c07b">Int</span><span style="color:#abb2bf">]</span> <span style="color:#c678dd">=</span> xs <span style="color:#c678dd">match</span>
  <span style="color:#c678dd">case</span> <span style="color:#e5c07b">Nil</span> <span style="color:#c678dd">=&gt;</span> <span style="color:#e5c07b">Nil</span>
  <span style="color:#c678dd">case</span> pivot <span style="color:#c678dd">::</span> rest <span style="color:#c678dd">=&gt;</span>
    <span style="color:#c678dd">val</span> <span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">,</span> more<span style="color:#abb2bf">)</span> <span style="color:#c678dd">=</span> rest<span style="color:#abb2bf">.</span>partition<span style="color:#abb2bf">(</span>_ <span style="color:#c678dd">&lt;</span> pivot<span style="color:#abb2bf">)</span>
    sort<span style="color:#abb2bf">(</span>less<span style="color:#abb2bf">)</span> <span style="color:#c678dd">++</span> <span style="color:#abb2bf">(</span>pivot <span style="color:#c678dd">::</span> sort<span style="color:#abb2bf">(</span>more<span style="color:#abb2bf">))</span>

<span style="color:#c678dd">val</span> nums <span style="color:#c678dd">=</span> <span style="color:#e5c07b">List</span><span style="color:#abb2bf">(</span><span style="color:#d19a66">5</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">2</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">8</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">1</span><span style="color:#abb2bf">,</span> <span style="color:#d19a66">9</span><span style="color:#abb2bf">)</span>
println<span style="color:#abb2bf">(</span>sort<span style="color:#abb2bf">(</span>nums<span style="color:#abb2bf">))</span>  <span style="color:#5c6370">// List(1, 2, 5, 8, 9)</span></code></pre>

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
