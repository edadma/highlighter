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
          |  default: << <span class="\class">\escape\text</span> >>
          |states
          |  root:
          |    &\S*?;     => entity
          |    <!--      => pl-c >comment
          |    <\s*(script)\s* => (pl-ent) >script-content >tag
          |    <\s*(style)\s* => (pl-ent) >style-content >tag
          |    <\s*([\w:.-]+) => (pl-ent) >tag
          |    <\s*/\s*([\w:.-]+)\s*> => (pl-ent)
          |  comment:
          |    [^-]+   => pl-c
          |    -->   => pl-c ^
          |    -   => pl-c
          |  tag:
          |    ([\w:-]+)\s*=\s* => (pl-e) >attr
          |    [\w:-]+ => pl-e
          |    /?\s*> => text ^
          |  style-content:
          |    <\s*/\s*(style)\s*> => (pl-ent) ^
          |    .+?(?=<\s*/\s*style\s*>) => pl-s1
          |  attr:
          |    (")(.*?)(") => (pl-pds pl-s pl-pds) ^
          |    (')(.*?)(') => (pl-pds pl-s pl-pds) ^
          |    [^\s>]+ => string ^
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