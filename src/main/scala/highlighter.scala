package xyz.hyperreal

import java.util.regex.Pattern

import scala.util.matching.Regex
import scala.util.parsing.input.Position

import xyz.hyperreal.backslash.{Command, Parser}


package object highlighter {

  class Const[T] {
    private var set = false
    private var value: T = _

    def apply( v: => T ) = {
      if (!set)
        synchronized {
          if (!set) {
            value = v
            set = true
          }
        }

      value
    }
  }

  def keywords( words: Seq[String], suffix: String = "\\b" ) =
    new Regex( words sortWith (_.length > _.length) mkString ("(?:", "|", ")" + suffix) )

  def symbols( words: Seq[String], suffix: String = "" ) =
    new Regex( words sortWith (_.length > _.length) map Pattern.quote mkString ("(?:", "|", ")" + suffix) )

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

  implicit def symbol2token( clas: Symbol ) = Token( clas.name )

  implicit def symbol2match( clas: Symbol ) = Seq(Match(clas) )

  implicit def symbol2state( state: Symbol ) = Push( state.name )

//  implicit def symbols2states( states: Seq[Symbol] ): Seq[Transition] = states map (s => Push( s.name ))

//  implicit def symbol2states( state: Symbol ): Seq[Transition] = Seq( Push(state.name) )

  implicit def transition2seq( transition: Transition ) = Seq( transition )

//  implicit def symbol2include( include: Symbol ) = null.asInstanceOf[Regex] -> IncludeRules( include.name )
  implicit def symbols2tokens( tokens: Seq[Symbol] ) = tokens map (s => Token( s.name ))

  implicit def strings2tokens( tokens: Seq[String] ) = tokens map (s => Token( s ))

  implicit def string2state( state: String ) = Push( state )

//  implicit def string2pattern( regex: String ) = Pattern.compile( regex )

  implicit def strings2states( states: Seq[String] ) = states map (s => Push( s ))

  implicit def string2token( clas: String ) =
    clas split "/" match {
      case Array( c ) => Token( c )
      case Array( c, s ) => Token( c, s )
      case _ => sys.error( s"bad class designator: $clas" )
    }

  val backslashParser = new Parser( Command.standard )

  implicit def map2template( templates: Map[String, String] ) =
    templates map { case (name, code) => (name, backslashParser.parse(io.Source.fromString(code))) }

  val Text = Token( "text", "text" )

//  implicit def tuple2rule( rule: (String, Symbol) ) = MatchRule( rule._1, rule._2 )

//  implicit def symbol2action( clas: Symbol ) = Action( clas )
}