package xyz.hyperreal.highlighter


object Main extends App {

  val input =
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
  val highlighter =
    new Highlighter {
      //trace = true

      def define = HighlighterParser(
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
          |classes
          |  keyword: kw
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