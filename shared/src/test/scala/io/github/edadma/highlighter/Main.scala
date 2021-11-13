package io.github.edadma.highlighter

object Main extends App {

//  val input = scala.io.Source.fromFile( "test.json" ) mkString
  val input =
    """
      |<p>{{ .firstname }} {{ .lastname }}</p>
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 0
      def define: Definition = HighlighterParser(scala.io.Source.fromFile("highlighters/squiggly.hl"))
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
