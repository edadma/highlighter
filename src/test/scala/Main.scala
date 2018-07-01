package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |val a = value
      |/* asdf
      | zxcv */
      |validate b = -456 // this is a comment
      |asdf c = 789
    """.stripMargin
  val highlighter =
    new Highlighter {
      //trace = true

      def define = HighlighterParser(
        """
          |highlighter
          |  name: basics
          |options
          |  regex: dotall multiline
          |templates
          |  default: {{ <\class\ "\escape\text"> }}
          |equates
          |  digits = '\d+'
          |  keywords = ['val',
          |    'validate']
          |states #states
          |  root:
          |    {{words( keywords +
          |      ['asdf'] )}}\b     => keyword
          |    {{digits}}  => number
          |    -{{digits}} => number
          |    \w+     => ident
          |    =       => symbol
          |    /\*     => comment >comment
          |    //.*?$  => comment
          |  comment:
          |    \*/   => comment ^
          |    .     => comment
        """.stripMargin
      )

      println( define )
    }

  println( highlighter.highlight(input) )

}

/*

<div class="highlight highlight-text-html-basic"><pre>&lt;!DOCTYPE html&gt;
&lt;<span class="pl-ent">html</span>&gt;
  &lt;<span class="pl-ent">head</span>&gt;
    &lt;<span class="pl-ent">style</span>&gt;<span class="pl-s1"></span>
<span class="pl-s1">      <span class="pl-ent">body</span> {<span class="pl-c1"><span class="pl-c1">background-color</span></span>: <span class="pl-c1">powderblue</span>;}</span>
<span class="pl-s1">      <span class="pl-ent">p</span>    {<span class="pl-c1"><span class="pl-c1">color</span></span>: <span class="pl-c1">red</span>;}</span>
<span class="pl-s1">    </span><span class="pl-s1">&lt;</span>/<span class="pl-ent">style</span>&gt;
  &lt;/<span class="pl-ent">head</span>&gt;
  &lt;<span class="pl-ent">body</span>&gt;
    <span class="pl-c"><span class="pl-c">&lt;!--</span> comment <span class="pl-c">--&gt;</span></span>
    &lt;<span class="pl-ent">p</span> <span class="pl-e">align</span>=<span class="pl-s"><span class="pl-pds">"</span>right<span class="pl-pds">"</span></span>&gt;paragraph&lt;/<span class="pl-ent">p</span>&gt;
  &lt;/<span class="pl-ent">body</span>&gt;
&lt;/<span class="pl-ent">html</span>&gt;</pre></div>

 */