package xyz.hyperreal.highlighter.highlighters

import xyz.hyperreal.backslash._
import xyz.hyperreal.highlighter._

import scala.util.parsing.input.OffsetPosition


object JSONHighlighter extends Highlighter {

  def define =
    Definition(List(
      InfoItems(List(Name("JSON"))), 
      Options(List(Dotall)), 
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
        "int_part" -> LiteralRAST("-?(0|[1-9]\\d*)"), 
        "frac_part" -> LiteralRAST("\\.\\d+"), 
        "exp_part" -> LiteralRAST("[eE][+-]?\\d+")
      )), 
      Includes(Map(
        "simplevalue" -> List(
          MatchRule(
            name = None,
            regex = StaticRAST("true|false|null)\\b"),
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
              VariableRAST("int_part"), 
              StaticRAST("("), 
              BinaryRAST(
                l = VariableRAST("frac_part"),
                op = "+",
                r = VariableRAST("exp_part")
              ), 
              StaticRAST("|"), 
              VariableRAST("exp_part"), 
              StaticRAST("|"), 
              VariableRAST("frac_part"), 
              StaticRAST(")")
            )),
            actions = List(
              Match(Token(
                clas = "Number.Float",
                template = "default"
              ))
            )
          ), 
          MatchRule(
            name = None,
            regex = VariableRAST("int_part"),
            actions = List(
              Match(Token(
                clas = "Number.Integer",
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
          )
        ), 
        "value" -> List(
          IncludeRule("simplevalue"), 
          MatchRule(
            name = None,
            regex = StaticRAST("\\{"),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              )), 
              Push("objectvalue")
            )
          ), 
          MatchRule(
            name = None,
            regex = StaticRAST("\\["),
            actions = List(
              Match(Token(
                clas = "Punctuation",
                template = "default"
              )), 
              Push("arrayvalue")
            )
          )
        )
      )), 
      States(List(
        State(
          name = "objectattribute",
          rules = List(
            IncludeRule("value"), 
            MatchRule(
              name = None,
              regex = StaticRAST(":"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST(","),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\}"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Popn(2)
              )
            )
          )
        ), 
        State(
          name = "objectvalue",
          rules = List(
            MatchRule(
              name = None,
              regex = StaticRAST("\"(\\\\\\\\|\\\\\"|[^\"])*\""),
              actions = List(
                Match(Token(
                  clas = "Name.Tag",
                  template = "default"
                )), 
                Push("objectattribute")
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\}"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            )
          )
        ), 
        State(
          name = "arrayvalue",
          rules = List(
            IncludeRule("value"), 
            MatchRule(
              name = None,
              regex = StaticRAST(","),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                ))
              )
            ), 
            MatchRule(
              name = None,
              regex = StaticRAST("\\]"),
              actions = List(
                Match(Token(
                  clas = "Punctuation",
                  template = "default"
                )), 
                Pop
              )
            )
          )
        ), 
        State(
          name = "root",
          rules = List(IncludeRule("value"))
        )
      ))
    ))

}
