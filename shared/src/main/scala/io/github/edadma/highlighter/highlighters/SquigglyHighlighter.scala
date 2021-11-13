package io.github.edadma.highlighter.highlighters

import io.github.edadma.backslash._
import io.github.edadma.highlighter._

import scala.util.parsing.input.OffsetPosition

object SquigglyHighlighter extends Highlighter {

  def define: Definition =
    Definition(
      List(
        InfoItems(List(Name("Squiggly"))),
        Options(List(Dotall, Ignorecase)),
        Templates(
          Map(
            "default" -> GroupAST(Vector(
              LiteralAST("<span class=\""),
              VariableAST("class"),
              LiteralAST("\">"),
              CommandAST(
                pos = null,
                c = io.github.edadma.backslash.Command.standard("escape"),
                args = List(VariableAST("text")),
                optional = Map()
              ),
              LiteralAST("</span>")
            ))
          )),
        Equates(Map(
          "IDENT" -> LiteralRAST("[\\p{Alpha}$_][\\p{Alpha}$_0-9]*"),
          "keywords" -> ListRAST(List(
            LiteralRAST("for"),
            LiteralRAST("with"),
            LiteralRAST("match"),
            LiteralRAST("case"),
            LiteralRAST("default"),
            LiteralRAST("if"),
            LiteralRAST("else"),
            LiteralRAST("then")
          ))
        )),
        States(List(
          State(
            name = "root",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("&\\S*?;"),
                actions = List(
                  Match(Token(
                    clas = "Name.Entity",
                    template = "default"
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("\\<\\!\\[CDATA\\[.*?\\]\\]\\>"),
                actions = List(
                  Match(Token(
                    clas = "Comment.Preproc",
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
                  )),
                  Push("comment")
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("<\\?.*?\\?>"),
                actions = List(
                  Match(Token(
                    clas = "Comment.Preproc",
                    template = "default"
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("<![^>]*>"),
                actions = List(
                  Match(Token(
                    clas = "Comment.Preproc",
                    template = "default"
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("(<)\\s*(script)\\s*"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Punctuation",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Tag",
                        template = "default"
                      )
                    )),
                  Push("script-content"),
                  Push("tag")
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("(<)\\s*(style)\\s*"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Punctuation",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Tag",
                        template = "default"
                      )
                    )),
                  Push("style-content"),
                  Push("tag")
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("(<)\\s*([\\w:.-]+)"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Punctuation",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Tag",
                        template = "default"
                      )
                    )),
                  Push("tag")
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("(<)\\s*(/)\\s*([\\w:.-]+)\\s*(>)"),
                actions = List(
                  Groups(List(
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Tag",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    )
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("\\{\\{"),
                actions = List(
                  Match(Token(
                    clas = "Comment.Preproc",
                    template = "default"
                  )),
                  Push("stag")
                )
              )
            )
          ),
          State(
            name = "stag",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("\\}\\}"),
                actions = List(
                  Match(Token(
                    clas = "Comment.Preproc",
                    template = "default"
                  )),
                  Pop
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
              )
            )
          ),
          State(
            name = "comment",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("[^-]+"),
                actions = List(
                  Match(Token(
                    clas = "Comment",
                    template = "default"
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("-->"),
                actions = List(
                  Match(Token(
                    clas = "Comment",
                    template = "default"
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("-"),
                actions = List(
                  Match(Token(
                    clas = "Comment",
                    template = "default"
                  ))
                )
              )
            )
          ),
          State(
            name = "tag",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("([\\w:-]+)\\s*(=)\\s*"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Name.Attribute",
                        template = "default"
                      ),
                      Token(
                        clas = "Operator",
                        template = "default"
                      )
                    )),
                  Push("attr")
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("[\\w:-]+"),
                actions = List(
                  Match(Token(
                    clas = "Name.Attribute",
                    template = "default"
                  ))
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("(/?)\\s*(>)"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Punctuation",
                        template = "default"
                      ),
                      Token(
                        clas = "Punctuation",
                        template = "default"
                      )
                    )),
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
                regex = StaticRAST("(<)\\s*(/)\\s*(script)\\s*(>)"),
                actions = List(
                  Groups(List(
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Tag",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    )
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST(".+?(?=<\\s*/\\s*script\\s*>)"),
                actions = List(
                  Match(Using("Javascript"))
                )
              )
            )
          ),
          State(
            name = "style-content",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("(<)\\s*(/)\\s*(style)\\s*(>)"),
                actions = List(
                  Groups(List(
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Tag",
                      template = "default"
                    ),
                    Token(
                      clas = "Punctuation",
                      template = "default"
                    )
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST(".+?(?=<\\s*/\\s*style\\s*>)"),
                actions = List(Match(Using("CSS")))
              )
            )
          ),
          State(
            name = "attr",
            rules = List(
              MatchRule(
                name = None,
                regex = StaticRAST("\".*?\""),
                actions = List(
                  Match(Token(
                    clas = "String",
                    template = "default"
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("'.*?'"),
                actions = List(
                  Match(Token(
                    clas = "String",
                    template = "default"
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("[^\\s>]+"),
                actions = List(
                  Match(Token(
                    clas = "String",
                    template = "default"
                  )),
                  Pop
                )
              )
            )
          )
        ))
      ))

}
