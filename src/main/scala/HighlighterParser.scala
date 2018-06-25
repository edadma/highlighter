//@
package xyz.hyperreal.highlighter

import java.util.regex.{MatchResult, Pattern}

import scala.util.parsing.combinator.{PackratParsers, RegexParsers}


object HighlighterParser extends RegexParsers {

  override val whiteSpace = """[ \t]+"""r

  def nl = rep1("\n")

  def onl = rep("\n")

  def definition =
    onl ~> rep1(section) ^^ Definition

  def section =
    optionSection | infoSection | templateSection | stateSection

  def optionSection =
    "options" ~> rep1(options) ^^ Options

  def options =
    "ignorecase" ^^^ Ignorecase |
    "dotall" ^^^ Dotall

  def infoSection =
    "definition" ~> nl ~> rep1sep(infoItems, nl) <~ onl ^^ InfoItems

  def infoItems =
    "name" ~> ":" ~> ident ^^ Name

  def ident = """[a-zA-Z]\w*"""r

  def templateSection =
    "templates" ~> nl ~> rep1sep(template, nl) <~ onl ^^ (ts => Templates( ts toMap ))

  def template =
    ident ~ ":" ~ "<<" ~ ".*(?=>>)".r ~ ">>" ^^ {
      case n ~ _ ~ _ ~ t ~ _ => (n, t.trim)
    }

  def stateSection =
    "states" ~> nl ~> rep1(state) ^^ States

  def state = ident ~ ":" ~ onl ~ rep1sep(rules, nl) ^^ {
    case name ~ _ ~ _ ~ rules => State( name, rules )
  }

  def rules =
    matchRule

  def matchRule = """.*(?==>)""".r ~ "=>" ~ rep1(action) ^^ {
    case r ~ _ ~ a => MatchRule( r.trim, a )
  }

  def action =
    ident ^^ (t => Match( t )) |
    ">" ~> ident ^^ Push |
    "^" ^^^ Pop

//  def groupsRule = """.*(?=\s*=>)""".r ~ "=>" ~ "(" ~ rep1(ident) ~ ")" ^^ {
//    case r ~ _ ~ t => GroupRule( r, List(t) )
//  }

  def apply( input: String ): Definition = parseAll( definition, input ) match {
    case Success( result, _ ) => result
    case failure : NoSuccess => scala.sys.error( failure.msg )
  }

}