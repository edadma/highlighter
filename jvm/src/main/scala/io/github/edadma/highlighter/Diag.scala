package io.github.edadma.highlighter

@main def diagLoadAll(): Unit =
  val dir = new java.io.File("/Users/ed/dev/juicer/docs/grammars")
  val files = dir.listFiles().filter(_.getName.endsWith(".tmLanguage.json")).sortBy(_.getName)
  println(s"loading ${files.length} grammars from $dir")
  for f <- files do
    val name = f.getName.stripSuffix(".tmLanguage.json")
    val txt  = scala.io.Source.fromFile(f).mkString
    val t0 = System.currentTimeMillis()
    val res = Highlighter.fromJson(txt, ClassMode("hl-"))
    val ms = System.currentTimeMillis() - t0
    val warns = res.toOption.map(_.loadWarnings.length).getOrElse(-1)
    println(f"  $name%-20s  ${ms}%5dms  warnings=${warns}")
