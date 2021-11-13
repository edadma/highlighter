package io.github.edadma.highlighter.highlighters

import io.github.edadma.backslash._
import io.github.edadma.highlighter._

import scala.util.parsing.input.OffsetPosition

object SquigglyHighlighter extends Highlighter {

  def define =
    Definition(
      sections = List(
        InfoItems(items = List(Name(s = "Squiggly"))),
        Options(options = List(Dotall, Ignorecase)),
        Templates(
          templates = Map(
            "default" -> GroupAST(
              statements = Vector(
                LiteralAST(v = "<span class=\""),
                VariableAST(name = "class"),
                LiteralAST(v = "\">"),
                CommandAST(
                  pos = null,
                  c = io.github.edadma.backslash.Command.standard("escape"),
                  args = List(VariableAST(name = "text")),
                  optional = Map()
                ),
                LiteralAST(v = "</span>")
              )
            )
          )
        ),
        Equates(
          equates = Map(
            "IDENT" -> LiteralRAST(v = "[\\p{Alpha}$_][\\p{Alpha}$_0-9]*"),
            "keywords" -> ListRAST(
              l = List(
                LiteralRAST(v = "for"),
                LiteralRAST(v = "with"),
                LiteralRAST(v = "match"),
                LiteralRAST(v = "case"),
                LiteralRAST(v = "if"),
                LiteralRAST(v = "else"),
                LiteralRAST(v = "elif"),
                LiteralRAST(v = "then"),
                LiteralRAST(v = "end"),
                LiteralRAST(v = "define"),
                LiteralRAST(v = "block"),
                LiteralRAST(v = "with"),
                LiteralRAST(v = "return")
              )
            ),
            "builtin" -> ListRAST(
              l = List(
                LiteralRAST(v = "abs"),
                LiteralRAST(v = "absURL"),
                LiteralRAST(v = "append"),
                LiteralRAST(v = "capitalize"),
                LiteralRAST(v = "ceil"),
                LiteralRAST(v = "compact"),
                LiteralRAST(v = "complement"),
                LiteralRAST(v = "contains"),
                LiteralRAST(v = "default"),
                LiteralRAST(v = "distinct"),
                LiteralRAST(v = "drop"),
                LiteralRAST(v = "dropRight"),
                LiteralRAST(v = "emojify"),
                LiteralRAST(v = "fileExists"),
                LiteralRAST(v = "filter"),
                LiteralRAST(v = "filterNot"),
                LiteralRAST(v = "findRE"),
                LiteralRAST(v = "floor"),
                LiteralRAST(v = "format"),
                LiteralRAST(v = "head"),
                LiteralRAST(v = "htmlEscape"),
                LiteralRAST(v = "intersect"),
                LiteralRAST(v = "join"),
                LiteralRAST(v = "last"),
                LiteralRAST(v = "length"),
                LiteralRAST(v = "lower"),
                LiteralRAST(v = "map"),
                LiteralRAST(v = "markdownify"),
                LiteralRAST(v = "max"),
                LiteralRAST(v = "min"),
                LiteralRAST(v = "now"),
                LiteralRAST(v = "newline_to_br"),
                LiteralRAST(v = "number"),
                LiteralRAST(v = "partial"),
                LiteralRAST(v = "prepend"),
                LiteralRAST(v = "print"),
                LiteralRAST(v = "println"),
                LiteralRAST(v = "querify"),
                LiteralRAST(v = "random"),
                LiteralRAST(v = "relURL"),
                LiteralRAST(v = "reverse")
              )
            )
          )
        ),
        States(
          states = List(
            State(
              name = "root",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "&\\S*?;"),
                  actions = List(Match(tok = Token(clas = "Name.Entity", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "\\<\\!\\[CDATA\\[.*?\\]\\]\\>"),
                  actions = List(Match(tok = Token(clas = "Comment.Preproc", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "<!--"),
                  actions = List(
                    Match(tok = Token(clas = "Comment", template = "default")),
                    Push(name = "comment")
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "<\\?.*?\\?>"),
                  actions = List(Match(tok = Token(clas = "Comment.Preproc", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "<![^>]*>"),
                  actions = List(Match(tok = Token(clas = "Comment.Preproc", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*(script)\\s*"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default")
                      )
                    ),
                    Push(name = "script-content"),
                    Push(name = "tag")
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*(style)\\s*"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default")
                      )
                    ),
                    Push(name = "style-content"),
                    Push(name = "tag")
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*([\\w:.-]+)"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default")
                      )
                    ),
                    Push(name = "tag")
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*(/)\\s*([\\w:.-]+)\\s*(>)"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default"),
                        Token(clas = "Punctuation", template = "default")
                      )
                    )
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "\\{\\{"),
                  actions = List(
                    Match(tok = Token(clas = "Punctuation.Template", template = "default")),
                    Push(name = "stag")
                  )
                )
              )
            ),
            State(
              name = "stag",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "\\}\\}"),
                  actions = List(
                    Match(tok = Token(clas = "Punctuation.Template", template = "default")),
                    Pop
                  )
                ),
                MatchRule(
                  name = None,
                  regex = SeqRAST(
                    seq = List(
                      StaticRAST(s = "("),
                      FunctionRAST(f = "words", args = List(VariableRAST(v = "keywords"))),
                      StaticRAST(s = ")\\b")
                    )
                  ),
                  actions = List(Match(tok = Token(clas = "Keyword", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(true|false|null)\\b"),
                  actions = List(Match(tok = Token(clas = "Keyword.Constant", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = SeqRAST(
                    seq = List(
                      StaticRAST(s = "("),
                      FunctionRAST(f = "words", args = List(VariableRAST(v = "builtin"))),
                      StaticRAST(s = ")\\b")
                    )
                  ),
                  actions = List(Match(tok = Token(clas = "Name.Builtin", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = VariableRAST(v = "IDENT"),
                  actions = List(Match(tok = Token(clas = "Name.Variable", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "\"(\\\\\\\\|\\\\\"|[^\"])*\""),
                  actions = List(Match(tok = Token(clas = "String.Double", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "'(\\\\\\\\|\\\\'|[^'])*'"),
                  actions = List(Match(tok = Token(clas = "String.Single", template = "default")))
                )
              )
            ),
            State(
              name = "comment",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "[^-]+"),
                  actions = List(Match(tok = Token(clas = "Comment", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "-->"),
                  actions = List(Match(tok = Token(clas = "Comment", template = "default")), Pop)
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "-"),
                  actions = List(Match(tok = Token(clas = "Comment", template = "default")))
                )
              )
            ),
            State(
              name = "tag",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "([\\w:-]+)\\s*(=)\\s*"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Name.Attribute", template = "default"),
                        Token(clas = "Operator", template = "default")
                      )
                    ),
                    Push(name = "attr")
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "[\\w:-]+"),
                  actions = List(Match(tok = Token(clas = "Name.Attribute", template = "default")))
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(/?)\\s*(>)"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Punctuation", template = "default")
                      )
                    ),
                    Pop
                  )
                )
              )
            ),
            State(
              name = "script-content",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*(/)\\s*(script)\\s*(>)"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default"),
                        Token(clas = "Punctuation", template = "default")
                      )
                    ),
                    Pop
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = ".+?(?=<\\s*/\\s*script\\s*>)"),
                  actions = List(Match(tok = Using(dependency = "Javascript")))
                )
              )
            ),
            State(
              name = "style-content",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "(<)\\s*(/)\\s*(style)\\s*(>)"),
                  actions = List(
                    Groups(
                      toks = List(
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Punctuation", template = "default"),
                        Token(clas = "Name.Tag", template = "default"),
                        Token(clas = "Punctuation", template = "default")
                      )
                    ),
                    Pop
                  )
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = ".+?(?=<\\s*/\\s*style\\s*>)"),
                  actions = List(Match(tok = Using(dependency = "CSS")))
                )
              )
            ),
            State(
              name = "attr",
              rules = List(
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "\".*?\""),
                  actions = List(Match(tok = Token(clas = "String", template = "default")), Pop)
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "'.*?'"),
                  actions = List(Match(tok = Token(clas = "String", template = "default")), Pop)
                ),
                MatchRule(
                  name = None,
                  regex = StaticRAST(s = "[^\\s>]+"),
                  actions = List(Match(tok = Token(clas = "String", template = "default")), Pop)
                )
              )
            )
          )
        )
      )
    )

}
