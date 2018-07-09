package xyz.hyperreal.highlighter


object Main extends App {

//  val input = io.Source.fromFile( "test.html" ) mkString
  val input =
    """
      |asdf
    """.stripMargin
  val highlighter =
    new Highlighter {
      trace = true
//      def define = HighlighterParser( io.Source.fromFile("highlighters/backslash.hl")
    def define =
      HighlighterParser(
        """
          |highlighter
          |    name: Javascript
          |options
          |    regex: dotall unicode multiline
          |templates
          |    default: {{ <span class="\class">\escape\text</span> }}
          |equates
          |    JS_IDENT = '[\p{Alpha}$_][\p{Alpha}$_0-9]*'
          |    keywords = [
          |        'for', 'in', 'while', 'do', 'break', 'return', 'continue', 'switch', 'case', 'default', 'if', 'else',
          |        'throw', 'try', 'catch', 'finally', 'new', 'delete', 'typeof', 'instanceof', 'void', 'yield', 'this', 'of'
          |        ]
          |    reserved = [
          |        'abstract', 'boolean', 'byte', 'char', 'class', 'const', 'debugger', 'double', 'enum', 'export',
          |        'extends', 'final', 'float', 'goto', 'implements', 'import', 'int', 'interface', 'long', 'native',
          |        'package', 'private', 'protected', 'public', 'short', 'static', 'super', 'synchronized', 'throws',
          |        'transient', 'volatile'
          |        ]
          |    builtin = [
          |        'Array', 'Boolean', 'Date', 'Error', 'Function', 'Math', 'netscape',
          |        'Number', 'Object', 'Packages', 'RegExp', 'String', 'Promise', 'Proxy', 'sun', 'decodeURI',
          |        'decodeURIComponent', 'encodeURI', 'encodeURIComponent',
          |        'Error', 'eval', 'isFinite', 'isNaN', 'isSafeInteger', 'parseFloat', 'parseInt',
          |        'document', 'this', 'window'
          |        ]
          |includes
          |    commentsandwhitespace:
          |        /\*.*?\*/ => Comment.Multiline
          |        #.*?\n => Comment.Single
          |    root:
          |        \A#! ?/.*?\n => Comment.Hashbang
          |        ^(?=\s|/|<!--) => Text >slashstartsregex
          |        include commentsandwhitespace
          |        (\.\d+|[0-9]+\.[0-9]*)([eE][-+]?[0-9]+)? => Number.Float
          |        0[bB][01]+ => Number.Bin
          |        0[oO][0-7]+ => Number.Oct
          |        0[xX][0-9a-fA-F]+ => Number.Hex
          |        [0-9]+ => Number.Integer
          |        \.\.\.|=\> => Punctuation
          |        \+\+|--|~|&&|\?|:|\|\||\\(?=\n)|(<<|>>>?|==?|!=?|[-<>+*%&|^/])=? => Operator >slashstartsregex
          |        [{(\[;,] => Punctuation >slashstartsregex
          |        [})\].] => Punctuation
          |        ({{ words( keywords ) }})\b => Keyword >slashstartsregex
          |        (var|let|with|function)\b => Keyword.Declaration >slashstartsregex
          |        ({{ words( reserved ) }})\b => Keyword.Reserved
          |        (true|false|null|NaN|Infinity|undefined)\b => Keyword.Constant
          |        ({{ words( builtin ) }})\b => Name.Builtin
          |        {{JS_IDENT}} => Name.Other
          |        "(\\\\|\\"|[^"])*" => String.Double
          |        '(\\\\|\\'|[^'])*' => String.Single
          |        ` => String.Backtick >interp
          |states
          |    slashstartsregex:
          |        include commentsandwhitespace
          |        /(\\.|[^[/\\\n]|\[(\\.|[^\]\\\n])*])+/([gimuy]+\b|\B)c=> String.Regex ^
          |        (?=/) => Text ^>badregex
          |        => ^
          |    badregex:
          |        \n => Text ^
          |    root:
          |        include root
          |    interp:
          |        ` => String.Backtick ^
          |        \\\\ => String.Backtick
          |        \\` => String.Backtick
          |        \$\{ => String.Interpol >interp-inside
          |        \$ => String.Backtick
          |        [^`\\$]+ => String.Backtick
          |    interp-inside:
          |        \} => String.Interpol ^
          |        include root
        """.stripMargin
      )
    }

//  Console.withOut( new java.io.FileOutputStream("htest1.html") ) {
    println( highlighter.highlight(input) )
//  }

}
