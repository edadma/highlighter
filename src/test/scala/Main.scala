package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |val a = 123
      |/* asdf
      | zxcv */
      |val b = 456
      |
      |one => two
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true

      def define = HighlighterParser(
        """
          |definition
          |  name: asdf
          |options
          |  regex: dotall
          |templates
          |  default: << <\class\ "\escape\text"> >>
          |states
          |  root:
          |    val   => keyword
          |    \d+   => number
          |    \w+   => ident
          |    \=\>  => symbol
          |    =     => symbol
          |    /\*   => comment >comment
          |  comment:
          |    \*/   => comment ^
          |    .     => comment
        """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}