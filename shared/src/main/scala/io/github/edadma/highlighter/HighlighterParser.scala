package io.github.edadma.highlighter

import scala.collection.mutable
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{PagedSeq, PagedSeqReader}

object HighlighterParser extends RegexParsers {

  override val whiteSpace: Regex = """(?:[ \t]|##.*)+""".r

  val equates = new mutable.HashMap[String, RAST]

  def nl: Parser[List[String]] = rep1("\n")

  def onl: Parser[List[String]] = rep("\n")

  def definition: Parser[Definition] =
    onl ~> rep1(section) ^^ Definition

  def section: Parser[Section] =
    optionSection | infoSection | templateSection | includeSection | stateSection | classesSection | equatesSection | extendsSection

  def extendsSection: Parser[Extends] =
    "extends" ~> ident <~ nl ^^ Extends

  def optionSection: Parser[Options] =
    "options" ~> nl ~> rep1(options) ^^ (o => Options(o.flatten))

  def options: Parser[List[LexerOption]] =
    "regex" ~> ":" ~> rep1(regexFlags) <~ onl

  def regexFlags: Parser[LexerOption] =
    "ignorecase" ^^^ Ignorecase |
      "dotall" ^^^ Dotall |
      "multiline" ^^^ Multiline |
      "unicode" ^^^ Unicode

  def infoSection: Parser[InfoItems] =
    "highlighter" ~> nl ~> rep1(infoItems) ^^ InfoItems

  def infoItems: Parser[InfoItem] =
    "name" ~> ":" ~> ident <~ onl ^^ Name |
      "alias" ~> ":" ~> ident <~ onl ^^ Alias

  def ident: Parser[String] = """[a-zA-Z_][\w._-]*""".r

  def number: Parser[Int] = """\d+""".r ^^ (_.toInt)

  def integer: Parser[Int] = """\d+""".r ^^ (_.toInt)

  def templateSection: Parser[Templates] =
    "templates" ~> nl ~> rep1(template) ^^ (ts => Templates(ts.toMap))

  def template: Parser[(String, String)] =
    ident ~ ":" ~ "{{%" ~ rep(guard(not("%}}")) ~> elem("", _ => true)) ~ "%}}" <~ onl ^^ {
      case n ~ _ ~ _ ~ t ~ _ => (n, t.mkString.trim)
    }

  def includeSection: Parser[Includes] =
    "includes" ~> nl ~> rep1(include) ^^ (includes => Includes(includes.toMap))

  def include: Parser[(String, List[Rule])] =
    ident ~ ":" ~ onl ~ rep1(rules <~ onl) ^^ {
      case name ~ _ ~ _ ~ rules => (name, rules)
    }

  def stateSection: Parser[States] =
    "states" ~> nl ~> rep1(state) ^^ States

  def state: Parser[State] =
    ident ~ ":" ~ onl ~ rep1(rules <~ onl) ^^ {
      case name ~ _ ~ _ ~ rules => State(name, rules)
    }

  def rules: Parser[Rule] =
    guard(not(ident ~ ":")) ~> (matchRule | includeRule | inheritRule | defaultRule)

  def matchRule: Parser[MatchRule] =
    opt("`" ~> ident <~ "`") ~ pattern ~ "=>" ~ rep1(action) ^^ {
      case n ~ r ~ _ ~ a => MatchRule(n, r, a)
    }

  def pattern: Parser[RAST] =
    rep1(guard(not("=>")) ~> segment) ^^ {
      case List(p) => p
      case s       => SeqRAST(s)
    }

  def segment: Parser[RAST] =
    guard(not("{{")) ~> """.+?(?=\s*(?:=>|\{\{))""".r ^^ StaticRAST |
      "{{" ~> code <~ "}}"

  def code: Parser[RAST] = additive

  def additive: Parser[RAST] =
    function ~ rep(("+" <~ onl) ~ function) ^^ {
      case a ~ l =>
        (l foldLeft a) {
          case (l, op ~ r) => BinaryRAST(l, op, r)
        }
    }

  def function: Parser[RAST] =
    ident ~ "(" ~ repsep(code, "," ~ onl) ~ ")" ^^ {
      case name ~ _ ~ args ~ _ => FunctionRAST(name, args)
    } |
      value

  def escapes(s: String): String =
    s.replace("""\"""", "\"")
      .replace("""\n""", "\n")
      .replace("""\t""", "\t")
      .replace("""\r""", "\r")
      .replace("""\\""", "\\")

  def value: Parser[RAST] =
    "\"" ~> """(\\\\|\\"|[^"])*""".r <~ "\"" ^^ (s => LiteralRAST(escapes(s))) |
      "'" ~> "(''|[^'])*".r <~ "'" ^^ (s => LiteralRAST(s.replace("''", "'"))) |
      "[" ~> onl ~> repsep(code, "," ~ onl) <~ onl <~ "]" ^^ ListRAST |
      ident ^^ VariableRAST

  def action: Parser[Action] =
    chars ^^ Match |
      "(" ~> rep1(chars) <~ ")" ^^ Groups |
      ">" ~> ident ^^ Push |
      "^" ~> integer ^^ Popn |
      "^" ^^^ Pop |
      "<" ~> ident ~ number <~ ">" ^^ {
        case v ~ g => Assign(v, g)
      }

  def chars: Parser[Chars] =
    "[" ~> ident <~ "]" ^^ Using |
      ident ~ "/" ~ ident ^^ { case c ~ _ ~ t => Token(c, t) } |
      ident ^^ (Token(_))

  def includeRule: Parser[IncludeRule] = "include" ~> ident ^^ IncludeRule

  def inheritRule: Parser[IncludeRule] = "inherit" ^^^ Inhe
  def defaultRule: Parser[DefaultRule] = "=>" ~> rep1(action) ^^ DefaultRule

  def classesSection: Parser[Classes] =
    "classes" ~> nl ~> rep1(mapping <~ onl) ^^ (ms => Classes(ms.toMap))

  def mapping: Parser[(String, String)] =
    ident ~ ":" ~ ident ^^ {
      case c1 ~ _ ~ c2 => (c1, c2)
    }

  def equatesSection: Parser[Equates] = "equates" ~> nl ~> rep1(equate <~ onl) ^^ (ms => Equates(ms.toMap))

  def equate: Parser[(String, RAST)] =
    opt("var") ~ ident ~ "=" ~ code ^^ {
      case None ~ e ~ _ ~ c    => (e, c)
      case Some(_) ~ e ~ _ ~ c => (e, MutableRAST(c))
    }

  def apply(input: scala.io.Source): Definition =
    parseAll(definition, new PagedSeqReader(PagedSeq.fromSource(input))) match {
      case Success(result, _) => result
      case Error(_, next)     => sys.error(next.pos.longString)
    }

  def apply(input: String): Definition = apply(scala.io.Source.fromString(input))

}
