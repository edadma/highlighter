Highlighter
===========

[![Build Status](https://www.travis-ci.org/edadma/highlighter.svg?branch=master)](https://www.travis-ci.org/edadma/highlighter)
[![Build status](https://ci.appveyor.com/api/projects/status/iophnk3cycjtf8px?svg=true)](https://ci.appveyor.com/project/edadma/highlighter)
[![Coverage Status](https://coveralls.io/repos/github/edadma/highlighter/badge.svg?branch=master)](https://coveralls.io/github/edadma/highlighter?branch=master)
[![License](https://img.shields.io/badge/license-ISC-blue.svg)](https://github.com/edadma/highlighter/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/latest_release-v0.1-orange.svg)](https://github.com/edadma/highlighter/releases/tag/v0.1)

*Highlighter* is a regular expression based syntax highlighter library written in [Scala](http://scala-lang.org). Highlighter is inspired by [Pygments](http://pygments.org/), and it is not difficult to port Pygments lexers to Highlighter.  Highlighter has a convenient definition language to make it easier to develop new highlighters.

Examples
--------

Here's a typical Highlighter definition.  This is a port of the [Pygments HTML Lexer](https://bitbucket.org/birkenfeld/pygments-main/src/default/pygments/lexers/html.py) without CSS and JavaScript highlighting.

### Definition

```
definition
  name: HTML
options
  regex: dotall ignorecase
templates
  default: << <span class="\class">\escape\text</span> >>
states
  root:
    &\S*?;     => entity
    \<\!\[CDATA\[.*?\]\]\>     => preproc
    <!--      => comment >comment
    <\?.*?\?>  => preproc
    <![^>]*> => preproc
    (<)\s*(script)\s* => (punct tag) >script-content >tag
    (<)\s*(style)\s* => (punct tag) >style-content >tag
    (<)\s*([\w:.-]+) => (punct tag) >tag
    (<)\s*(/)\s*([\w:.-]+)\s*(>) => (punct punct tag punct)
  comment:
    [^-]+   => comment
    -->   => comment ^
    -   => comment
  tag:
    ([\w:-]+)\s*(=)\s* => (attr oper) >attr
    [\w:-]+ => attr
    (/?)\s*(>) => (punct punct) ^
  script-content:
    (<)\s*(/)\s*(script)\s*(>) => (punct punct tag punct) ^
    .+?(?=<\s*/\s*script\s*>) => using-javascript
  style-content:
    (<)\s*(/)\s*(style)\s*(>) => (punct punct tag punct) ^
    .+?(?=<\s*/\s*style\s*>) => using-css
  attr:
    ".*?" => string ^
    '.*?' => string ^
    [^\s>]+ => string ^
```

### Input

```
<!DOCTYPE html>
<html>
  <head>
    <style>
      body {background-color: powderblue;}
      p    {color: red;}
    </style>
  </head>
  <body>
    <!-- comment -->
    <p align="right">paragraph</p>
  </body>
</html>
```

### Output

```html
<!DOCTYPE html>
<html>
  <head>
    <style>
      body {background-color: powderblue;}
      p    {color: red;}
    </style>
  </head>
  <body>
    <!-- comment -->
    <p align="right">paragraph</p>
  </body>
</html>
```

<div class="highlight highlight-text-html-basic"><pre>
<!DOCTYPE html>
<span class="pl-ent">html</span>>
  <span class="pl-ent">head</span>>
    <span class="pl-ent">style</span>><span class="pl-s1">
      body {background-color: powderblue;}
      p    {color: red;}
    </span><span class="pl-ent">style</span>
  <span class="pl-ent">head</span>
  <span class="pl-ent">body</span>>
    <span class="pl-c">&lt;!-- comment --&gt;</span>
    <span class="pl-ent">p</span> <span class="pl-e">align</span><span class="pl-pds">"</span><span class="pl-s">right</span><span class="pl-pds">"</span>>paragraph<span class="pl-ent">p</span>
  <span class="pl-ent">body</span>
<span class="pl-ent">html</span>
</pre></div>

### Library

This example program shows how to create a custom command to output an HTML unordered list, and also demonstrates a Highlighter `\for` loop.

```scala
import scala.util.parsing.input.Position

import xyz.hyperreal.highlighter._


object Example extends App {

  val input =
    """
      |<h2>Vaudeville Acts</h2>
      |<ol>
      |  \for \in act acts {
      |    <li>
      |      <h3>\act.name</h3>
      |      \list \act.members
      |    </li>
      |  }
      |</ol>
    """.trim.stripMargin
  val acts =
    List(
      Map(
        "name" -> "Three Stooges",
        "members" -> List( "Larry", "Moe", "Curly" )
      ),
      Map(
        "name" -> "Andrews Sisters",
        "members" -> List( "LaVerne", "Maxine", "Patty" )
      ),
      Map(
        "name" -> "Abbott and Costello",
        "members" -> List( "William (Bud) Abbott", "Lou Costello" )
      )
    )
  val customCommand =
    new Command( "list", 1 ) {
      def apply( pos: Position, rendered: Renderer, args: List[Any], context: AnyRef ) = {
        val list = args.head.asInstanceOf[List[String]]

        s"<ul>${list map (item => s"<li>$item</li>") mkString}</ul>"
      }
    }

  val parser = new Parser( Command.standard ++ Map("list" -> customCommand) )
  val renderer = new Renderer( parser, Map() )

  renderer.render( parser.parse(io.Source.fromString(input)), Map("acts" -> acts), Console.out )
}
```

This program prints

```html
<h2>Vaudeville Acts</h2>
<ol>

    <li>
      <h3>Three Stooges</h3>
      <ul><li>Larry</li><li>Moe</li><li>Curly</li></ul></li>

    <li>
      <h3>Andrews Sisters</h3>
      <ul><li>LaVerne</li><li>Maxine</li><li>Patty</li></ul></li>

    <li>
      <h3>Abbott and Costello</h3>
      <ul><li>William (Bud) Abbott</li><li>Lou Costello</li></ul></li>

</ol>
```

### Executable

This next example shows how to use *highlighter* as an executable from the command line.

```bash
echo "testing \join \v \", \"" | java -jar highlighter-0.1.jar -j "{v: [\"one\", \"two\", \"three\"]}" --
```

The above command prints

    testing one, two, three


Usage
-----

### Library

Use the following definition to use Highlighter in your Maven project:

```xml
<repository>
  <id>hyperreal</id>
  <url>https://dl.bintray.com/edadma/maven</url>
</repository>

<dependency>
  <groupId>xyz.hyperreal</groupId>
  <artifactId>highlighter</artifactId>
  <version>0.1</version>
</dependency>
```

Add the following to your `build.sbt` file to use Highlighter in your SBT project:

```sbt
resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies += "xyz.hyperreal" %% "highlighter" % "0.1"
```

### Executable

An executable can be downloaded from [here](https://dl.bintray.com/edadma/generic/highlighter-0.1.jar). *You do not need* the Scala library for it to work because the JAR already contains all dependencies. You just need Java 8+ installed.

Run it as a normal Java executable JAR with the command `java -jar highlighter-0.1.jar <template>` in the folder where you downloaded the file, where *template* is the name of the template file to be rendered.

Building
--------

### Requirements

- Java 8
- SBT 1.1.5+
- Scala 2.12.6+

### Clone and Assemble Executable

```bash
git clone git://github.com/edadma/highlighter.git
cd highlighter
sbt assembly
```

The command `sbt assembly` also runs all the unit tests.


License
-------

ISC © 2018 Edward A. Maxedon, Sr.