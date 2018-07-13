package xyz.hyperreal.highlighter


object Main extends App {

//  val input = io.Source.fromFile( "test.html" ) mkString
  val input =
    """
      |// asdf
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 0
      def define = HighlighterParser( io.Source.fromFile("highlighters/javascript.hl") )
//    def define =
//      HighlighterParser(
//        """
//          |
//        """.stripMargin
//      )
    }

//  Console.withOut( new java.io.FileOutputStream("htest1.html") ) {
    println( highlighter.highlight(input) )
//  }

}
