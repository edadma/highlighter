package xyz.hyperreal.highlighter

import java.util.regex.Pattern

import xyz.hyperreal.backslash.AST


case class Definition( sections: Seq[Section] )

trait Section
case class InfoItems( items: Seq[InfoItem] ) extends Section
case class Options( options: Seq[LexerOption] ) extends Section
case class Templates( templates: Map[String, AST] ) extends Section
case class States( states: Seq[State] ) extends Section
case class Includes( includes: Map[String, Seq[Rule]] ) extends Section
case class Classes( classes: Map[String, String] ) extends Section

trait InfoItem
case class Name( s: String ) extends InfoItem

case class State( name: String, rules: Seq[Rule] )

trait LexerOption
case object Ignorecase extends LexerOption
case object Dotall extends LexerOption

trait Rule
case class MatchRule( regex: String, actions: Seq[Action] ) extends Rule { val pattern = new Const[Pattern] }
case class MismatchRule( regex: String, actions: Seq[Action] ) extends Rule { val pattern = new Const[Pattern] }
case class DefaultRule( actions: Seq[Action] ) extends Rule
case class IncludeRule( include: String ) extends Rule { val rules = new Const[Seq[Rule]] }

trait Action
case class Match( tok: Token ) extends Action { val ast = new Const[AST] }
case class Groups( toks: Seq[Token] ) extends Action { val asts = Seq.fill( toks.length )( new Const[AST] ) }
case class Using( fs: Seq[Either[Token, Highlighter]] ) extends Action
case class Push( name: String ) extends Action { val state = new Const[State] }
case object Pop extends Action
case class Popn( n: Int ) extends Action
case class Transition( name: String ) extends Action { val state = new Const[State] }
case class Output( s: String, tok: Token ) extends Action

case class Token( clas: String, template: String = "default" )