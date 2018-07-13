package xyz.hyperreal.highlighter.highlighters

import xyz.hyperreal.backslash._
import xyz.hyperreal.highlighter._

import scala.util.parsing.input.OffsetPosition


object JavascriptHighlighter extends Highlighter {

  def define =
    Definition(List(
      InfoItems(List(Name("Javascript"))), 
      Options(List(
        Dotall, 
        Unicode, 
        Multiline
      )), 
      Templates(Map(
        "default" -> GroupAST(Vector(
          LiteralAST("<span class=\""), 
          VariableAST("class"), 
          LiteralAST("\">"), 
          CommandAST(
            pos = OffsetPosition(
              source = "<span class=\"\\class\">\\escape\\text</span>",
              offset = 21
            ),
            c = xyz.hyperreal.backslash.Command.standard("escape"),
            args = List(VariableAST("text")),
            optional = Map()
          ), 
          LiteralAST("</span>")
        ))
      )), 
      Equates(Map(
        "JS_IDENT" -> LiteralRAST("[\\p{Alpha}$_][\\p{Alpha}$_0-9]*"), 
        "keywords" -> ListRAST(List(
          LiteralRAST("for"), 
          LiteralRAST("in"), 
          LiteralRAST("while"), 
          LiteralRAST("do"), 
          LiteralRAST("break"), 
          LiteralRAST("return"), 
          LiteralRAST("continue"), 
          LiteralRAST("switch"), 
          LiteralRAST("case"), 
          LiteralRAST("default"), 
          LiteralRAST("if"), 
          LiteralRAST("else"), 
          LiteralRAST("throw"), 
          LiteralRAST("try"), 
          LiteralRAST("catch"), 
          LiteralRAST("finally"), 
          LiteralRAST("new"), 
          LiteralRAST("delete"), 
          LiteralRAST("typeof"), 
          LiteralRAST("instanceof"), 
          LiteralRAST("void"), 
          LiteralRAST("yield"), 
          LiteralRAST("this"), 
          LiteralRAST("of")
        )), 
        "reserved" -> ListRAST(List(
          LiteralRAST("abstract"), 
          LiteralRAST("boolean"), 
          LiteralRAST("byte"), 
          LiteralRAST("char"), 
          LiteralRAST("class"), 
          LiteralRAST("const"), 
          LiteralRAST("debugger"), 
          LiteralRAST("double"), 
          LiteralRAST("enum"), 
          LiteralRAST("export"), 
          LiteralRAST("extends"), 
          LiteralRAST("final"), 
          LiteralRAST("float"), 
          LiteralRAST("goto"), 
          LiteralRAST("implements"), 
          LiteralRAST("import"), 
          LiteralRAST("int"), 
          LiteralRAST("interface"), 
          LiteralRAST("long"), 
          LiteralRAST("native"), 
          LiteralRAST("package"), 
          LiteralRAST("private"), 
          LiteralRAST("protected"), 
          LiteralRAST("public"), 
          LiteralRAST("short"), 
          LiteralRAST("static"), 
          LiteralRAST("super"), 
          LiteralRAST("synchronized"), 
          LiteralRAST("throws"), 
          LiteralRAST("transient"), 
          LiteralRAST("volatile")
        )), 
        "builtin" -> ListRAST(List(
          LiteralRAST("Array"), 
          LiteralRAST("Boolean"), 
          LiteralRAST("Date"), 
          LiteralRAST("Error"), 
          LiteralRAST("Function"), 
          LiteralRAST("Math"), 
          LiteralRAST("netscape"), 
          LiteralRAST("Number"), 
          LiteralRAST("Object"), 
          LiteralRAST("Packages"), 
          LiteralRAST("RegExp"), 
          LiteralRAST("String"), 
          LiteralRAST("Promise"), 
          LiteralRAST("Proxy"), 
          LiteralRAST("sun"), 
          LiteralRAST("decodeURI"), 
          LiteralRAST("decodeURIComponent"), 
          LiteralRAST("encodeURI"), 
          LiteralRAST("encodeURIComponent"), 
          LiteralRAST("Error"), 
          LiteralRAST("eval"), 
          LiteralRAST("isFinite"), 
          LiteralRAST("isNaN"), 
          LiteralRAST("isSafeInteger"), 
          LiteralRAST("parseFloat"), 
          LiteralRAST("parseInt"), 
          LiteralRAST("document"), 
          LiteralRAST("this"), 
          LiteralRAST("window")
        ))
      )), 
      Includes(Map(
        "commentsandwhitespace" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("\\s+"),
            actions = List(
              Match(Token(
                clas = "Text",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("<!--"),
            actions = List(
              Match(Token(
                clas = "Comment",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("/\\*.*?\\*/"),
            actions = List(
              Match(Token(
                clas = "Comment.Multiline",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("#.*?\\n"),
            actions = List(
              Match(Token(
                clas = "Comment.Single",
                template = "default"
              ))
            )
          )
        ), 
        "root" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("\\A#! ?/.*?\\n"),
            actions = List(
              Match(Token(
                clas = "Comment.Hashbang",
                template = "default"
              ))
            )
          ), 
          IncludeRule("commentsandwhitespace"), 
          MatchRule(
            name = None,
            regex = StaticRAST("(\\.\\d+|[0-9]+\\.[0-9]*)([eE][-+]?[0-9]+)?"),
            actions = List(
              Match(Token(
                clas = "Number.Float",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("0[bB][01]+"),
            actions = List(
              Match(Token(
                clas = "Number.Bin",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("0[oO][0-7]+"),
            actions = List(
              Match(Token(
                clas = "Number.Oct",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("0[xX][0-9a-fA-F]+"),
            actions = List(
              Match(Token(
                clas = "Number.Hex",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[0-9]+"),
            actions = List(
              Match(Token(
                clas = "Number.Integer",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\\.\\.\\.|=\\>"),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\\+\\+|--|~|&&|\\?|:|\\|\\||\\\\(?=\\n)|(<<|>>>?|==?|!=?|[-<>+*%&|^/])=?"),
            actions = List(
              Match(Token(
                clas = "Operator",
                template = "default"
              )), 
              Push("slashstartsregex")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[{(\\[;,]"),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              )), 
              Push("slashstartsregex")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("[})\\].]"),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = SeqRAST(List(
              StaticRAST("("), 
              FunctionRAST(
                f = "words",
                args = List(VariableRAST("keywords"))
              ), 
              StaticRAST(")\\b")
            )),
            actions = List(
              Match(Token(
                clas = "Keyword",
                template = "default"
              )), 
              Push("slashstartsregex")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(var|let|with|function)\\b"),
            actions = List(
              Match(Token(
                clas = "Keyword.Declaration",
                template = "default"
              )), 
              Push("slashstartsregex")
            )
          ), 
          MatchRule(
            name = None,
            regex = SeqRAST(List(
              StaticRAST("("), 
              FunctionRAST(
                f = "words",
                args = List(VariableRAST("reserved"))
              ), 
              StaticRAST(")\\b")
            )),
            actions = List(
              Match(Token(
                clas = "Keyword.Reserved",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("(true|false|null|NaN|Infinity|undefined)\\b"),
            actions = List(
              Match(Token(
                clas = "Keyword.Constant",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = SeqRAST(List(
              StaticRAST("("), 
              FunctionRAST(
                f = "words",
                args = List(VariableRAST("builtin"))
              ), 
              StaticRAST(")\\b")
            )),
            actions = List(
              Match(Token(
                clas = "Name.Builtin",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = VariableRAST("JS_IDENT"),
            actions = List(
              Match(Token(
                clas = "Name.Other",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\"(\\\\\\\\|\\\\\"|[^\"])*\""),
            actions = List(
              Match(Token(
                clas = "String.Double",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("'(\\\\\\\\|\\\\'|[^'])*'"),
            actions = List(
              Match(Token(
                clas = "String.Single",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("`"),
            actions = List(
              Match(Token(
                clas = "String.Backtick",
                template = "default"
              )), 
              Push("interp")
            )
          )
        )
      )), 
      States(List(
        State(
          name = "slashstartsregex",
          rules = List(
            IncludeRule("commentsandwhitespace"), 
            MatchRule(
              name = None,
              regex = StaticRAST("/(\\\\.|[^[/\\\\\\n]|\\[(\\\\.|[^\\]\\\\\\n])*])+/([gimuy]+\\b|\\B)"),
              actions = List(
                Match(Token(
                  clas = "String.Regex",
                  template = "default"
                )), 
                Pop
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("(?=/)"),
              actions = List(
                Match(Token(
                  clas = "Text",
                  template = "default"
                )), 
                Pop, 
                Push("badregex")
              )
            ), 
            DefaultRule(List(Pop))
          )
        ), 
        State(
          name = "badregex",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("\\n"),
              actions = List(
                Match(Token(
                  clas = "Text",
                  template = "default"
                )), 
                Pop
              )
            )
          )
        ), 
        State(
          name = "root",
          rules = List(IncludeRule("root"))
        ), 
        State(
          name = "interp",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("`"),
              actions = List(
                Match(Token(
                  clas = "String.Backtick",
                  template = "default"
                )), 
                Pop
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\\\\\\\"),
              actions = List(
                Match(Token(
                  clas = "String.Backtick",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\\\`"),
              actions = List(
                Match(Token(
                  clas = "String.Backtick",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\$\\{"),
              actions = List(
                Match(Token(
                  clas = "String.Interpol",
                  template = "default"
                )), 
                Push("interp-inside")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\$"),
              actions = List(
                Match(Token(
                  clas = "String.Backtick",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("[^`\\\\$]+"),
              actions = List(
                Match(Token(
                  clas = "String.Backtick",
                  template = "default"
                ))
              )
            )
          )
        ), 
        State(
          name = "interp-inside",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("\\}"),
              actions = List(
                Match(Token(
                  clas = "String.Interpol",
                  template = "default"
                )), 
                Pop
              )
            ), 
            IncludeRule("root")
          )
        )
      ))
    ))

}
