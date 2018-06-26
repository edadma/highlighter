package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |<em>asdf</em>
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
          |    &\S*?;     => entity
          |    \<\!\[CDATA\[.*?\]\]\>     => preproc
          |    <!--      => comment >comment
          |    <\?.*?\?>  => preproc
          |    <![^>]*> => preproc
          |    (<)\s*(script)\s* => (punct tag) >script-content >tag
          |    (<)\s*(style)\s* => (punct tag) >style-content >tag
          |    (<)\s*([\w:.-]+) => (punct tag) >tag
          |    (<)\s*(/)\s*([\w:.-]+)\s*(>) => (punct punct tag punct)
          |  comment:
          |    \*/   => comment ^
          |    .     => comment
          |  tag:
          |    ([\w:-]+)\s*(=)\s* => (attr oper)
          |    [\w:-]+ => attr
          |    (/?)\s*(>) => (punct punct) ^
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