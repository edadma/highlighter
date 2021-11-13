package io.github.edadma.highlighter

import java.util.regex.Pattern

import io.github.edadma.backslash.AST

case class Definition(sections: Seq[Section])

trait Section
case class InfoItems(items: Seq[InfoItem]) extends Section
case class Options(options: Seq[LexerOption]) extends Section
case class Templates(templates: Map[String, AST]) extends Section
case class States(states: Seq[State]) extends Section
case class Includes(includes: Map[String, Seq[Rule]]) extends Section
case class Classes(classes: Map[String, String]) extends Section
case class Equates(equates: Map[String, RAST]) extends Section

trait InfoItem
case class Name(s: String) extends InfoItem

case class State(name: String, rules: Seq[Rule])

trait LexerOption
case object Ignorecase extends LexerOption
case object Dotall extends LexerOption
case object Multiline extends LexerOption
case object Unicode extends LexerOption

trait Rule { val actions: Seq[Action] }
case class MatchRule(name: Option[String], var regex: RAST, actions: Seq[Action]) extends Rule {
  val pattern = new Const[Pattern]
}
case class MismatchRule(regex: RAST, actions: Seq[Action]) extends Rule { val pattern = new Const[Pattern] }
case class DefaultRule(actions: Seq[Action]) extends Rule
case class IncludeRule(include: String) extends Rule {
  val actions: Seq[Action] = Nil; val rules = new Const[Seq[Rule]]
}

trait Action
case class Match(tok: Chars) extends Action { val ast = new Const[AST] }
case class Groups(toks: Seq[Chars]) extends Action { val asts: Seq[Const[AST]] = Seq.fill(toks.length)(new Const[AST]) }
case class Push(name: String) extends Action { val state = new Const[State] }
case object Pop extends Action
case class Popn(n: Int) extends Action
case class Transition(name: String) extends Action { val state = new Const[State] }
case class Output(s: String, tok: Chars) extends Action
case class Alter(name: String, regex: RAST) extends Action
case class Assign(name: String, group: Int) extends Action

trait Chars
case class Using(dependency: String) extends Chars { val highlighter = new Const[Highlighter] }
case class Token(clas: String, template: String = "default") extends Chars
