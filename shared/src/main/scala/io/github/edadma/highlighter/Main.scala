package io.github.edadma.highlighter

import scala.io.Source

@main def run(args: String*): Unit =
  val grammarJson = Source.fromFile("test-grammars/javascript.tmLanguage.json").mkString

  Highlighter.fromJson(grammarJson, InlineMode(Theme.OneDark)) match
    case Right(hl) =>
      val code = """function greet(name) {
  const message = `Hello, ${name}!`;
  if (name === "world") {
    console.log(message); // greeting
  }
  return 42;
}"""
      println(hl.highlight(code))
    case Left(err) =>
      println(s"Parse error: $err")
