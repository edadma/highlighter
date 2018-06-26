package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |<!DOCTYPE html>
      |<html>
      |  <head>
      |    <style>
      |      body {background-color: powderblue;}
      |      p    {color: red;}
      |    </style>
      |  </head>
      |  <body>
      |    <!-- comment -->
      |    <p align="right">paragraph</p>
      |  </body>
      |</html>
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
          |    [^-]+   => comment
          |    -->   => comment ^
          |    -   => comment
          |  tag:
          |    ([\w:-]+)\s*(=)\s* => (attr oper) >attr
          |    [\w:-]+ => attr
          |    (/?)\s*(>) => (punct punct) ^
          |  script-content:
          |    (<)\s*(/)\s*(script)\s*(>) => (punct punct tag punct) ^
          |    .+?(?=<\s*/\s*script\s*>) => using-javascript
          |  style-content:
          |    (<)\s*(/)\s*(style)\s*(>) => (punct punct tag punct) ^
          |    .+?(?=<\s*/\s*style\s*>) => using-css
          |  attr:
          |    ".*?" => string ^
          |    '.*?' => string ^
          |    [^\s>]+ => string ^
        """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}