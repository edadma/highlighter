package io.github.edadma.highlighter.highlighters

import io.github.edadma.highlighter._

import scala.util.parsing.input.OffsetPosition

object BackslashHighlighter extends Highlighter {

  def define =
    Definition(
      List(
        InfoItems(List(Name("Backslash"))),
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
          "commands1" -> ListRAST(List(
            LiteralRAST("abs"),
            LiteralRAST("ceil"),
            LiteralRAST("distinct"),
            LiteralRAST("downcase"),
            LiteralRAST("escape"),
            LiteralRAST("escapeFull"),
            LiteralRAST("escapeOnce"),
            LiteralRAST("floor"),
            LiteralRAST("head"),
            LiteralRAST("include"),
            LiteralRAST("integer"),
            LiteralRAST("last"),
            LiteralRAST("lit"),
            LiteralRAST("markdown"),
            LiteralRAST("negate"),
            LiteralRAST("nil"),
            LiteralRAST("normalize"),
            LiteralRAST("number"),
            LiteralRAST("reverse"),
            LiteralRAST("round"),
            LiteralRAST("size"),
            LiteralRAST("sort"),
            LiteralRAST("string"),
            LiteralRAST("tail"),
            LiteralRAST("timestamp"),
            LiteralRAST("trim"),
            LiteralRAST("u"),
            LiteralRAST("upcase")
          )),
          "commands2" -> ListRAST(List(
            LiteralRAST("*"),
            LiteralRAST("+"),
            LiteralRAST("-"),
            LiteralRAST("/"),
            LiteralRAST("/="),
            LiteralRAST("<"),
            LiteralRAST("<="),
            LiteralRAST("="),
            LiteralRAST(">"),
            LiteralRAST(">="),
            LiteralRAST("^"),
            LiteralRAST("append"),
            LiteralRAST("contains"),
            LiteralRAST("date"),
            LiteralRAST("default"),
            LiteralRAST("drop"),
            LiteralRAST("join"),
            LiteralRAST("max"),
            LiteralRAST("min"),
            LiteralRAST("range"),
            LiteralRAST("rem"),
            LiteralRAST("remove"),
            LiteralRAST("removeFirst"),
            LiteralRAST("split"),
            LiteralRAST("take")
          )),
          "commands3" -> ListRAST(
            List(
              LiteralRAST("replace"),
              LiteralRAST("replaceFirst"),
              LiteralRAST("slice")
            )),
          "command" -> LiteralRAST("(\\\\)((?:[a-zA-Z_][a-zA-Z_.]*| |[^a-zA-Z0-9_\\s]+))")
        )),
        Includes(Map(
          "html" -> List(
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
            )
          ),
          "backslash" -> List(
            MatchRule(
              name = None,
              regex = SeqRAST(
                List(
                  StaticRAST("(\\\\)("),
                  FunctionRAST(
                    f = "words",
                    args = List(
                      VariableRAST("commands1")
                    )
                  ),
                  StaticRAST("\\b)")
                )),
              actions = List(
                Groups(
                  List(
                    Token(
                      clas = "Keyword.Reserved",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Builtin",
                      template = "default"
                    )
                  )),
                Push("command")
              )
            ),
            MatchRule(
              name = None,
              regex = SeqRAST(
                List(
                  StaticRAST("(\\\\)("),
                  FunctionRAST(
                    f = "words",
                    args = List(
                      VariableRAST("commands2")
                    )
                  ),
                  StaticRAST("\\b)")
                )),
              actions = List(
                Groups(
                  List(
                    Token(
                      clas = "Keyword.Reserved",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Builtin",
                      template = "default"
                    )
                  )),
                Push("command"),
                Push("command")
              )
            ),
            MatchRule(
              name = None,
              regex = SeqRAST(
                List(
                  StaticRAST("(\\\\)("),
                  FunctionRAST(
                    f = "words",
                    args = List(
                      VariableRAST("commands3")
                    )
                  ),
                  StaticRAST("\\b)")
                )),
              actions = List(
                Groups(
                  List(
                    Token(
                      clas = "Keyword.Reserved",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Builtin",
                      template = "default"
                    )
                  )),
                Push("command"),
                Push("command"),
                Push("command")
              )
            ),
            MatchRule(
              name = None,
              regex = VariableRAST("command"),
              actions = List(
                Groups(
                  List(
                    Token(
                      clas = "Keyword.Reserved",
                      template = "default"
                    ),
                    Token(
                      clas = "Name.Builtin",
                      template = "default"
                    )
                  ))
              )
            ),
            MatchRule(
              name = None,
              regex = StaticRAST("\\|"),
              actions = List(
                Match(Token(
                  clas = "Keyword.Reserved",
                  template = "default"
                )),
                Push("pipe")
              )
            )
          )
        )),
        States(List(
          State(
            name = "root",
            rules = List(
              IncludeRule("html"),
              IncludeRule("backslash"),
              MatchRule(
                name = None,
                regex = StaticRAST("\\{|\\}"),
                actions = List(
                  Match(Token(
                    clas = "Keyword.Reserved",
                    template = "default"
                  ))
                )
              )
            )
          ),
          State(
            name = "command",
            rules = List(
              MatchRule(
                name = None,
                regex = SeqRAST(
                  List(
                    StaticRAST("(\\\\)("),
                    FunctionRAST(
                      f = "words",
                      args = List(
                        VariableRAST("commands1")
                      )
                    ),
                    StaticRAST("\\b)")
                  )),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    ))
                )
              ),
              MatchRule(
                name = None,
                regex = SeqRAST(
                  List(
                    StaticRAST("(\\\\)("),
                    FunctionRAST(
                      f = "words",
                      args = List(
                        VariableRAST("commands2")
                      )
                    ),
                    StaticRAST("\\b)")
                  )),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Push("command")
                )
              ),
              MatchRule(
                name = None,
                regex = SeqRAST(
                  List(
                    StaticRAST("(\\\\)("),
                    FunctionRAST(
                      f = "words",
                      args = List(
                        VariableRAST("commands3")
                      )
                    ),
                    StaticRAST("\\b)")
                  )),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Push("command"),
                  Push("command")
                )
              ),
              MatchRule(
                name = None,
                regex = VariableRAST("command"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("-?\\d+(\\.\\d+)?|0x[0-9a-fA-F]+"),
                actions = List(
                  Match(Token(
                    clas = "Number.Integer",
                    template = "default"
                  )),
                  Pop
                )
              ),
              MatchRule(
                name = None,
                regex = StaticRAST("\\{"),
                actions = List(
                  Match(Token(
                    clas = "Keyword.Reserved",
                    template = "default"
                  )),
                  Pop,
                  Push("argument")
                )
              )
            )
          ),
          State(
            name = "argument",
            rules = List(
              IncludeRule("html"),
              IncludeRule("backslash"),
              MatchRule(
                name = None,
                regex = StaticRAST("\\{"),
                actions = List(
                  Match(Token(
                    clas = "Keyword.Reserved",
                    template = "default"
                  )),
                  Pop
                )
              )
            )
          ),
          State(
            name = "pipe",
            rules = List(
              MatchRule(
                name = None,
                regex = SeqRAST(
                  List(
                    StaticRAST("(\\\\)("),
                    FunctionRAST(
                      f = "words",
                      args = List(
                        VariableRAST("commands2")
                      )
                    ),
                    StaticRAST("\\b)")
                  )),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Push("command")
                )
              ),
              MatchRule(
                name = None,
                regex = SeqRAST(
                  List(
                    StaticRAST("(\\\\)("),
                    FunctionRAST(
                      f = "words",
                      args = List(
                        VariableRAST("commands3")
                      )
                    ),
                    StaticRAST("\\b)")
                  )),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Push("command"),
                  Push("command")
                )
              ),
              MatchRule(
                name = None,
                regex = VariableRAST("command"),
                actions = List(
                  Groups(
                    List(
                      Token(
                        clas = "Keyword.Reserved",
                        template = "default"
                      ),
                      Token(
                        clas = "Name.Builtin",
                        template = "default"
                      )
                    )),
                  Pop
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
