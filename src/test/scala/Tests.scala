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
        |highlighter
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
        |highlighter
        |  name: HTML
        |options
        |  regex: dotall ignorecase
        |templates
        |  default: << <span class="\class">\escape\text</span> >>
        |states
        |  root:
        |    &\S*?;                       => entity
        |    \<\!\[CDATA\[.*?\]\]\>       => preproc
        |    <!--                         => comment >comment
        |    <\?.*?\?>                    => preproc
        |    <![^>]*>                     => preproc
        |    (<)\s*(script)\s*            => (punct tag) >script-content >tag
        |    (<)\s*(style)\s*             => (punct tag) >style-content >tag
        |    (<)\s*([\w:.-]+)             => (punct tag) >tag
        |    (<)\s*(/)\s*([\w:.-]+)\s*(>) => (punct punct tag punct)
        |  comment:
        |    [^-]+ => comment
        |    -->   => comment ^
        |    -     => comment
        |  tag:
        |    ([\w:-]+)\s*(=)\s* => (attr oper) >attr
        |    [\w:-]+            => attr
        |    (/?)\s*(>)         => (punct punct) ^
        |  script-content:
        |    (<)\s*(/)\s*(script)\s*(>) => (punct punct tag punct) ^
        |    .+?(?=<\s*/\s*script\s*>)  => using-javascript
        |  style-content:
        |    (<)\s*(/)\s*(style)\s*(>) => (punct punct tag punct) ^
        |    .+?(?=<\s*/\s*style\s*>)  => using-css
        |  attr:
        |    ".*?"   => string ^
        |    '.*?'   => string ^
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
        |    <!-- comment -->
        |    <p align="right">paragraph</p>
        |  </body>
        |</html>
      """.stripMargin
    ).trim shouldBe
    """
      |<span class="preproc">&lt;!DOCTYPE html&gt;</span>
      |<span class="punct">&lt;</span><span class="tag">html</span><span class="punct">&gt;</span>
      |  <span class="punct">&lt;</span><span class="tag">head</span><span class="punct">&gt;</span>
      |    <span class="punct">&lt;</span><span class="tag">style</span><span class="punct">&gt;</span><span class="using-css">
      |      body {background-color: powderblue;}
      |      p    {color: red;}
      |    </span><span class="punct">&lt;/</span><span class="tag">style</span><span class="punct">&gt;</span>
      |  <span class="punct">&lt;/</span><span class="tag">head</span><span class="punct">&gt;</span>
      |  <span class="punct">&lt;</span><span class="tag">body</span><span class="punct">&gt;</span>
      |    <span class="comment">&lt;!-- comment --&gt;</span>
      |    <span class="punct">&lt;</span><span class="tag">p</span> <span class="attr">align</span><span class="oper">=</span><span class="string">"right"</span><span class="punct">&gt;</span>paragraph<span class="punct">&lt;/</span><span class="tag">p</span><span class="punct">&gt;</span>
      |  <span class="punct">&lt;/</span><span class="tag">body</span><span class="punct">&gt;</span>
      |<span class="punct">&lt;/</span><span class="tag">html</span><span class="punct">&gt;</span>
    """.stripMargin.trim
  }

  "includes" in {
    highlight(
      """
        |options
        |  regex: multiline
        |templates
        |  default: << <span class="\class">\escape\text</span> >>
        |includes
        |  comments:
        |    /\*.*?\*/ => comment
        |    //.*?$ => comment
        |states
        |  root:
        |    include comments
        |    (function)\s+(\w+)\s+(\{) => (keyword name keyword) >function
        |  function:
        |    include comments
        |    \} => keyword ^
      """.stripMargin,
      """
        |function asdf {
        |  // doesn't do anything;
        |}
        |
        |// comment
        |
        |function zxcv {
        |  /* useless aswell; */
        |  something stupid;
        |}
      """.trim.stripMargin
    ).trim shouldBe
      """
        |<span class="keyword">function</span><span class="name">asdf</span><span class="keyword">{</span>
        |  <span class="comment">// doesn't do anything;</span>
        |<span class="keyword">}</span>
        |
        |<span class="comment">// comment</span>
        |
        |<span class="keyword">function</span><span class="name">zxcv</span><span class="keyword">{</span>
        |  <span class="comment">/* useless aswell; */</span>
        |  something stupid;
        |<span class="keyword">}</span>
      """.stripMargin.trim

  }
}