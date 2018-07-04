package xyz.hyperreal.highlighter


object Main extends App {

  val input = io.Source.fromFile( "test.html" ) mkString
  val highlighter = highlighters.HTMLHighlighter

  Console.withOut( new java.io.FileOutputStream("htest1.html") ) {
    println( highlighter.highlight(input) )
  }

}
