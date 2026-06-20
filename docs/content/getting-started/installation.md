---
title: "Installation"
weight: 1
---

highlighter is cross-published for the JVM, Scala.js, and Scala Native. Add it with the `%%%`
operator so sbt selects the right platform artifact:

```scala
libraryDependencies += "io.github.edadma" %%% "highlighter" % "0.0.10"
```

It is published to Maven Central, so no extra resolver is needed.

## Requirements

highlighter is a pure-Scala library — its only dependencies are
[`zio-json`](https://github.com/zio/zio-json) for reading grammar files and the pure-Scala
[`oniguruma`](https://github.com/edadma/oniguruma) regex engine. There is **no native
dependency**, so the same code links and runs on the JVM, in the browser through Scala.js, and
as a Scala Native binary.

## Importing

Everything lives in one package:

```scala
import io.github.edadma.highlighter.*
```

That brings in `Highlighter`, the render modes (`ClassMode`, `InlineMode`), `Theme`, and the
`Token` type. The next page highlights a snippet end to end.
