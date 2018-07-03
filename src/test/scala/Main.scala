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
          |    &\S*?;                        => Name.Entity
          |    \<\!\[CDATA\[.*?\]\]\>        => Comment.Preproc
          |    <!--                          => Comment >comment
          |    <\?.*?\?>                     => Comment.Preproc
          |    <![^>]*>                      => Comment.Preproc
          |    (<)\s*(script)\s*             => (Punctuation Name.Tag) >script-content >tag
          |    (<)\s*(style)\s*              => (Punctuation Name.Tag) >style-content >tag
          |    (<)\s*([\w:.-]+)              => (Punctuation Name.Tag) >tag
          |    (<)\s*(/)\s*([\w:.-]+)\s*(>)  => (Punctuation Punctuation Name.Tag Punctuation)
          |  comment:
          |    [^-]+  => Comment
          |    -->    => Comment ^
          |    -      => Comment
          |  tag:
          |    ([\w:-]+)\s*(=)\s*  => (Name.Attribute Operator) >attr
          |    [\w:-]+             => Name.Attribute
          |    (/?)\s*(>)          => (Punctuation Punctuation) ^
          |  script-content:
          |    (<)\s*(/)\s*(script)\s*(>)  => (Punctuation Punctuation Name.Tag Punctuation) ^
          |    .+?(?=<\s*/\s*script\s*>)   => [javascript]
          |  style-content:
          |    (<)\s*(/)\s*(style)\s*(>)  => (Punctuation Punctuation Name.Tag Punctuation) ^
          |    .+?(?=<\s*/\s*style\s*>)   => [css]
          |  attr:
          |    ".*?"    => String ^
          |    '.*?'    => String ^
          |    [^\s>]+  => String ^
        """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}
