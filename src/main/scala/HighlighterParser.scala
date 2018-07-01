//@
package xyz.hyperreal.highlighter

import java.util.regex.Pattern

import scala.collection.mutable
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{PagedSeq, PagedSeqReader}


object HighlighterParser extends RegexParsers {

  override val whiteSpace = """[ \t]+"""r

  val equates = new mutable.HashMap[String, RAST]

  def nl = rep1("\n")

  def onl = rep("\n")

  def definition =
    onl ~> rep1(section) ^^ Definition

  def section =
    optionSection | infoSection | templateSection | includeSection | stateSection | classesSection | equatesSection

  def optionSection =
    "options" ~> nl ~> rep1(options) ^^ (o => Options( o.flatten ))

  def options =
    "regex" ~> ":" ~> rep1(regexFlags) <~ onl

  def regexFlags =
    "ignorecase" ^^^ Ignorecase |
    "dotall" ^^^ Dotall |
    "multiline" ^^^ Multiline

  def infoSection =
    "highlighter" ~> nl ~> rep1(infoItems) ^^ InfoItems

  def infoItems =
    "name" ~> ":" ~> ident <~ onl ^^ Name

  def ident = """[a-zA-Z][\w-]*"""r

  def templateSection =
    "templates" ~> nl ~> rep1(template) ^^ (ts => Templates( ts toMap ))

  def template =
    ident ~ ":" ~ "{{" ~ rep(guard(not("}}")) ~> elem("", ch => true)) ~ "}}" <~ onl ^^ {
      case n ~ _ ~ _ ~ t ~ _ => (n, t.mkString.trim)
    }

  def includeSection =
    "includes" ~> nl ~> rep1(include) ^^ (includes => Includes( includes toMap ))

  def include =
    ident ~ ":" ~ onl ~ rep1(rules <~ onl) ^^ {
      case name ~ _ ~ _ ~ rules => (name, rules)
    }

  def stateSection =
    "states" ~> nl ~> rep1(state) ^^ States

  def state =
    ident ~ ":" ~ onl ~ rep1(rules <~ onl) ^^ {
      case name ~ _ ~ _ ~ rules => State( name, rules )
    }

  def rules =
    matchRule | includeRule

  def matchRule =
    guard(not(ident ~ ":")) ~> pattern ~ "=>" ~ rep1(action) ^^ {
      case r ~ _ ~ a => MatchRule( r, a )
    }

  def pattern =
    rep1(guard(not("=>")) ~> segment)

  def segment =
    guard(not("{{")) ~> """.*?(?==>|\{\{)""".r ^^ StaticRAST |
    "{{" ~> code <~ "}}"

  def code: Parser[RAST] =
    function | value

  def function =
    ident ~ "(" ~ repsep(code, "," ~ onl) ~ ")" ^^ {
      case name ~ _ ~ args ~ _ => FunctionRAST( name, args )
    }

  def value =
    "'" ~> "[^']*".r <~ "'" ^^ LiteralRAST |
    "[" ~> repsep(code, "," ~ onl) <~ "]" ^^ ListRAST |
    ident ^^ VariableRAST

  def action =
    token ^^ Match |
    "(" ~> rep1(token) <~ ")" ^^ Groups |
    ">" ~> ident ^^ Push |
    "^" ^^^ Pop

  def token =
    ident ~ "/" ~ ident ^^ { case c ~ _ ~ t => Token( c, t ) } |
    ident ^^ (c => Token( c, "default" ))

  def includeRule =
    "include" ~> ident ^^ IncludeRule

  def classesSection =
    "classes" ~> nl ~> rep1(mapping <~ onl) ^^ (ms => Classes( ms toMap ))

  def mapping =
    ident ~ ":" ~ ident ^^ {
      case c1 ~ _ ~ c2 => (c1, c2)
    }

  def equatesSection =
    "equates" ~> nl ~> rep1(equate <~ onl) ^^ (ms => Equates( ms toMap ))

  def equate =
    ident ~ "=" ~ code ^^ {
      case e ~ _ ~ c => (e, c)
    }

  def apply( input: io.Source ): Definition =
    parseAll( definition, new PagedSeqReader(PagedSeq.fromSource(input)) ) match {
      case Success( result, _ ) => result
      case Error( msg, next ) => sys.error( next.pos.longString )
    }

  def apply( input: String ): Definition = apply( io.Source.fromString(input) )

}