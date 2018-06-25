package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |val a = 123
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true

      def define = HighlighterParser(
        """
          |definition
          |  name: asdf
          |templates
          |  default: << <\class\ "\escape\text"> >>
          |states
          |  root: val  => keyword
          |        \d+  => number
          |        \w+  => ident
          |        =    => symbol
          |        /*   => comment >comment
          |  comment: */  => comment ^
          |           .   => comment
          |
        """.stripMargin
      )
//      override def define: Definition =
//        Definition(
//          Seq(
//            Templates(
//              Map[String, String](
//                "default" -> """<\class\ "\escape\text">"""
//              )
//            ),
//            States(
//              Seq(
//                State( "root",
//                  Seq(
//                    MatchRule( "val", 'keyword ),
//                    """\d+""" -> 'number,
//                    """\w+""" -> 'ident
//                  )
//                )
//              )
//            )
//          )
//        )
    }

  println( highlighter.highlight(input) )

}