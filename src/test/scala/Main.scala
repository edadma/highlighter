package xyz.hyperreal.highlighter

object Main extends App {

  val input = io.Source.fromFile( "test.json" ) mkString
//  val input =
//    """
//      |{
//      |  "products": [
//      |    {
//      |      "name": "RCA 32\u2033 ROKU SMART TV",
//      |      "price": 207.00,
//      |      "inStock": true
//      |    },
//      |    {
//      |      "name": "LG 55UK6300",
//      |      "price": 1098.00,
//      |      "inStock": false
//      |    }
//      |  ]
//      |}
//    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 0
      def define = HighlighterParser( io.Source.fromFile("highlighters/json.hl") )
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
