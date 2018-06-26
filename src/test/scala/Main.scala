package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |val a = 123
      |/* asdf
      | zxcv */
      |val b = 456 // this is a comment
      |val c = 789
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true

      def define = HighlighterParser(
        """
          |definition
          |  name: HTML
          |options
          |  regex: dotall ignorecase
          |templates
          |  default: << <\class\ "\escape\text"> >>
          |states
          |  root:
          |    [^<&]+     => text
          |    &\S*?;     => entity
          |    \<\!\[CDATA\[.*?\]\]\>     => preproc
          |    <!--      => comment >comment
          |    <\?.*?\?>  => preproc
          |    <![^>]*> => preproc
          |    (<)\s*(script)\s* => (punct tag) >script-content >tag
          |  comment:
          |    \*/   => comment ^
          |    .     => comment
        """.stripMargin
//      """
//        |definition
//        |  name: HTML
//        |options
//        |  regex: dotall ignorecase
//        |templates
//        |  default: << <\class\ "\escape\text"> >>
//        |states
//        |  root:
//        |    [^<&]+     => text
//        |    &\S*?;     => entity
//        |    \<\!\[CDATA\[.*?\]\]\>     => preproc
//        |    <!--      => comment >comment
//        |    <\?.*?\?>  => preproc
//        |    <![^>]*> => preproc
//        |    (<)\s*(script)\s* => (punct tag) >script-content >tag
//        |  comment:
//        |    \*/   => comment ^
//        |    .     => comment
//      """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}