//@
package xyz.hyperreal.highlighter

import java.util.regex.{MatchResult, Pattern}

import scala.util.parsing.combinator.{PackratParsers, RegexParsers}


object HighlighterParser extends RegexParsers {

  def definition =
    rep1(section) ^^ Definition

  def section =
    optionSection | infoSection | templateSection | stateSection

  def optionSection =
    "options" ~> rep1(options) ^^ Options

  def options =
    "ignorecase" ^^^ Ignorecase |
    "dotall" ^^^ Dotall

  def infoSection =
    "definition" ~> rep1(infoItems) ^^ InfoItems

  def infoItems =
    "name" ~> ":" ~> ident ^^ Name

  def ident = """[a-zA-Z]\w*"""r

  def templateSection =
    "templates" ~> rep1(template) ^^ (ts => Templates( ts toMap ))

  def template =
    ident ~ ":" ~ "<<" ~ ".*(?=>>)".r ~ ">>" ^^ {
      case n ~ _ ~ _ ~ t ~ _ => (n, t.trim)
    }

  def stateSection =
    "states" ~> rep1(state) ^^ States

  def state = ident ~ ":" ~ rep1(rules) ^^ {
    case name ~ _ ~ rules => State( name, rules )
  }

  def rules =
    matchRule

  def matchRule = """.*(?==>)""".r ~ "=>" ~ ident ^^ {
    case r ~ _ ~ t => MatchRule( r.trim, List(Match(t)) )
  }

//  def groupsRule = """.*(?=\s*=>)""".r ~ "=>" ~ "(" ~ rep1(ident) ~ ")" ^^ {
//    case r ~ _ ~ t => GroupRule( r, List(t) )
//  }

  def apply( input: String ): Definition = parseAll( definition, input ) match {
    case Success( result, _ ) => result
    case failure : NoSuccess => scala.sys.error( failure.msg )
  }

}