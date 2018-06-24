//@
package xyz.hyperreal.highlighter

import java.util.regex.{MatchResult, Pattern}

import scala.util.parsing.combinator.{PackratParsers, RegexParsers}


object HighlighterParser extends RegexParsers {

  def definition =
    rep1(section) ^^ Definition

  def section =
    optionSection | infoSection | stateSection

  def optionSection =
    "options" ~> rep1(options) ^^ Options

  def options =
    "ignorecase" ^^^ Ignorecase |
    "dotall" ^^^ Dotall

  def infoSection =
    "definition" ~> rep1(infoItems) ^^ InfoItems

  def infoItems =
    "name" ~> ident ^^ Name

  def ident = """[a-zA-Z]\w*"""r

  def stateSection =
    "states" ~> rep1(state)

  def state = ident ~ rep1(rule) ^^ {
    case name ~ rules => State( name, rules )
  }

  def rule = """.*(?=\s*=>)""".r ~ "=>" ~ ident ^^ {
    case r ~ _ ~ t => MatchRule( r, List(t) )
  }
  
  def apply( input: String ): Definition = parseAll( definition, input ) match {
    case Success( result, _ ) => result
    case failure : NoSuccess => scala.sys.error( failure.msg )
  }

}