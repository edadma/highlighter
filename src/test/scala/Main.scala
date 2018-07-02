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

      def define = HighlighterParser(
        """
          |highlighter
          |  name: HTML
          |options
          |  regex: dotall ignorecase
          |templates
          |  default: {{ <span class="\class">\escape\text</span> }}
          |states
          |  root:
          |    (&)(\S*?)(;)                 => (entitydelim entity entitydelim)
          |    \<\!\[CDATA\[.*?\]\]\>       => preproc
          |    <!--                         => comment >comment
          |    <\?.*?\?>                    => preproc
          |    <![^>]*>                     => preproc
          |    (<)\s*(script)\s*            => (tagdelim tag) >script-content >tag
          |    (<)\s*(style)\s*             => (tagdelim tag) >style-content >tag
          |    (<)\s*([\w:.-]+)             => (tagdelim tag) >tag
          |    (<)\s*(/)\s*([\w:.-]+)\s*(>) => (tagdelim tagdelim tag tagdelim)
          |  comment:
          |    [^-]+ => comment
          |    -->   => comment ^
          |    -     => comment
          |  tag:
          |    ([\w:-]+)\s*(=)\s* => (attr oper) >attr
          |    [\w:-]+            => attr
          |    (/?)\s*(>)         => (tagdelim tagdelim) ^
          |  script-content:
          |    (<)\s*(/)\s*(script)\s*(>) => (tagdelim tagdelim tag tagdelim) ^
          |    .+?(?=<\s*/\s*script\s*>)  => javascript
          |  style-content:
          |    (<)\s*(/)\s*(style)\s*(>) => (tagdelim tagdelim tag tagdelim) ^
          |    .+?(?=<\s*/\s*style\s*>)  => [css]
          |  attr:
          |    (")(.*?)(") => (stringdelim string stringdelim) ^
          |    (')(.*?)(') => (stringdelim string stringdelim) ^
          |    [^\s>]+     => string ^
        """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}
