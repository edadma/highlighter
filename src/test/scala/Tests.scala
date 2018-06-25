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
	
}