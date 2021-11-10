package io.github.edadma.highlighter

object Main extends App {

//  val input = scala.io.Source.fromFile( "test.json" ) mkString
  val input =
    """
      |\set product \{name "Nice TV" price 1049.00}
      |
      |The product name is \product.name.
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 0
      def define = HighlighterParser(scala.io.Source.fromFile("highlighters/backslash.hl"))
//    def define =
//      HighlighterParser(
//        """
//          |
//        """.stripMargin
//      )
    }

//  Console.withOut( new java.io.FileOutputStream("htest1.html") ) {
  println(highlighter.highlight(input))
//  }

}
