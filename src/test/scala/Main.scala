package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |<!DOCTYPE html>
      |<html>
      |    <head>
      |        <style>
      |          body {background-color: powderblue;}
      |          p    {color: red;}
      |        </style>
      |    </head>
      |    <body>
      |        <!-- comment -->
      |        <p align="right">&lt;paragraph&gt;</p>
      |    </body>
      |</html>
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true

      dependencies += ("css" -> new Highlighter {
        override def define: Definition = HighlighterParser( io.Source.fromFile("highlighters/css.hl") )
      })

      def define = HighlighterParser( io.Source.fromFile("highlighters/html.hl") )
    }

  println( highlighter.highlight(input) )

}
