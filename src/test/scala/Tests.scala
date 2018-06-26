//@
package xyz.hyperreal.highlighter

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {

  def highlight( defn: String, input: String ) = {
    val highlighter =
      new Highlighter {
        def define = HighlighterParser( defn )
      }

    highlighter.highlight( input )
  }

	"basics" in {
    highlight(
      """
        |definition
        |  name: basics
        |options
        |  regex: dotall multiline
        |templates
        |  default: << <\class\ "\escape\text"> >>
        |states
        |  root:
        |    val     => keyword
        |    \d+     => number
        |    \w+     => ident
        |    =       => symbol
        |    /\*     => comment >comment
        |    //.*?$  => comment
        |  comment:
        |    \*/   => comment ^
        |    .     => comment
      """.stripMargin,
      """
        |val a = 123
        |/* asdf
        | zxcv */
        |val b = 456 // this is a comment
        |val c = 789
      """.stripMargin
    ).trim shouldBe
      """
        |<keyword "val"> <ident "a"> <symbol "="> <number "123">
        |<comment "/* asdf
        | zxcv */">
        |<keyword "val"> <ident "b"> <symbol "="> <number "456"> <comment "// this is a comment">
        |<keyword "val"> <ident "c"> <symbol "="> <number "789">
      """.stripMargin.trim
	}

  "html" in {
    highlight(
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
        |    ([\w:-]+)\s*(=)\s* => (attr oper) attr
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
      """.stripMargin,
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
        |  <-- comment -->
        |  <p align="right">paragraph</p>
        |  </body>
        |</html>
      """.stripMargin
    ).trim shouldBe
    """
      |
    """.stripMargin.trim
  }
}