package xyz.hyperreal.highlighter


object Main extends App {

//  val input = io.Source.fromFile( "test.html" ) mkString
  val input =
    """
      |Array.prototype.quick_sort = function () {
      |    if (this.length < 2) { return this; }
      |
      |    var pivot = this[Math.round(this.length / 2)];
      |
      |    return this.filter(x => x <  pivot)
      |        .quick_sort()
      |        .concat(this.filter(x => x == pivot))
      |        .concat(this.filter(x => x >  pivot).quick_sort());
      |};
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true
      tracelimit = 15
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
