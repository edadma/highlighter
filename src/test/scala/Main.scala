package xyz.hyperreal.highlighter


object Main extends App {

//  val input = io.Source.fromFile( "test.html" ) mkString
  val input =
    """
      |\asdf
    """.stripMargin
  val highlighter =
    new Highlighter {
      def define = HighlighterParser( io.Source.fromFile("highlighters/backslash.hl") )
    }

//  Console.withOut( new java.io.FileOutputStream("htest1.html") ) {
    println( highlighter.highlight(input) )
//  }

}
