package xyz.hyperreal.highlighter.highlighters

import xyz.hyperreal.backslash._
import xyz.hyperreal.highlighter._

import scala.util.parsing.input.OffsetPosition


object HTMLHighlighter extends Highlighter {

  def define =
    Definition(List(
      InfoItems(List(Name("HTML"))),
      Options(List(Dotall, Ignorecase)),
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
      States(List(
        State(
          name = "root",
          rules = List(
            MatchRule(
              regex = List(StaticRAST("&\\S*?;")),
              actions = List(
                Match(Token(
                  clas = "Name.Entity",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(
                StaticRAST("\\<\\!\\[CDATA\\[.*?\\]\\]\\>")
              ),
              actions = List(
                Match(Token(
                  clas = "Comment.Preproc",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(StaticRAST("<!--")),
              actions = List(
                Match(Token(
                  clas = "Comment",
                  template = "default"
                )),
                Push("comment")
              )
            ),
            MatchRule(
              regex = List(
                StaticRAST("<\\?.*?\\?>")
              ),
              actions = List(
                Match(Token(
                  clas = "Comment.Preproc",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(StaticRAST("<![^>]*>")),
              actions = List(
                Match(Token(
                  clas = "Comment.Preproc",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(
                StaticRAST("(<)\\s*(script)\\s*")
              ),
              actions = List(
                Groups(List(
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
              regex = List(
                StaticRAST("(<)\\s*(style)\\s*")
              ),
              actions = List(
                Groups(List(
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
              regex = List(
                StaticRAST("(<)\\s*([\\w:.-]+)")
              ),
              actions = List(
                Groups(List(
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
              regex = List(
                StaticRAST("(<)\\s*(/)\\s*([\\w:.-]+)\\s*(>)")
              ),
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
            )
          )
        ),
        State(
          name = "comment",
          rules = List(
            MatchRule(
              regex = List(StaticRAST("[^-]+")),
              actions = List(
                Match(Token(
                  clas = "Comment",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(StaticRAST("-->")),
              actions = List(
                Match(Token(
                  clas = "Comment",
                  template = "default"
                )),
                Pop
              )
            ),
            MatchRule(
              regex = List(StaticRAST("-")),
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
              regex = List(
                StaticRAST("([\\w:-]+)\\s*(=)\\s*")
              ),
              actions = List(
                Groups(List(
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
              regex = List(StaticRAST("[\\w:-]+")),
              actions = List(
                Match(Token(
                  clas = "Name.Attribute",
                  template = "default"
                ))
              )
            ),
            MatchRule(
              regex = List(
                StaticRAST("(/?)\\s*(>)")
              ),
              actions = List(
                Groups(List(
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
              regex = List(
                StaticRAST("(<)\\s*(/)\\s*(script)\\s*(>)")
              ),
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
              regex = List(
                StaticRAST(".+?(?=<\\s*/\\s*script\\s*>)")
              ),
              actions = List(
                Match(Using("javascript"))
              )
            )
          )
        ),
        State(
          name = "style-content",
          rules = List(
            MatchRule(
              regex = List(
                StaticRAST("(<)\\s*(/)\\s*(style)\\s*(>)")
              ),
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
              regex = List(
                StaticRAST(".+?(?=<\\s*/\\s*style\\s*>)")
              ),
              actions = List(Match(Using("css")))
            )
          )
        ),
        State(
          name = "attr",
          rules = List(
            MatchRule(
              regex = List(StaticRAST("\".*?\"")),
              actions = List(
                Match(Token(
                  clas = "String",
                  template = "default"
                )),
                Pop
              )
            ),
            MatchRule(
              regex = List(StaticRAST("'.*?'")),
              actions = List(
                Match(Token(
                  clas = "String",
                  template = "default"
                )),
                Pop
              )
            ),
            MatchRule(
              regex = List(StaticRAST("[^\\s>]+")),
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