package io.github.edadma.highlighter

object Main extends App {

//  val input = scala.io.Source.fromFile( "test.json" ) mkString
  val input =
    """
      |<!DOCTYPE html>
      |<html>
      |  <head>
      |    <style>
      |      body {background-color: powderblue;}
      |      p    {color: red;}
      |    </style>
      |  </head>
      |  <body>
      |    <!-- comment -->
      |    <p align="right">&lt;paragraph&gt;</p>
      |  </body>
      |</html>
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 0
      def define: Definition = HighlighterParser(scala.io.Source.fromFile("highlighters/html.hl"))
//    def define =
//      HighlighterParser(
//        """
//          |
//        """.stripMargin
//      )
    }

  Console.withOut(new java.io.FileOutputStream("example1.html")) {
    println(highlighter.highlight(input))
  }

}
