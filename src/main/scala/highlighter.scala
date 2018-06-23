package xyz.hyperreal

import java.util.regex.Pattern

import scala.util.matching.Regex
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

  implicit def symbol2token( clas: Symbol ) = Token( clas.name )

  implicit def symbol2state( state: Symbol ) = PushState( state.name )

  implicit def symbols2states( states: Seq[Symbol] ): Seq[Transition] = states map (s => PushState( s.name ))

  implicit def symbol2states( state: Symbol ): Seq[Transition] = Seq( PushState(state.name) )

  implicit def transition2seq( transition: Transition ) = Seq( transition )

  implicit def symbol2include( include: Symbol ) = null.asInstanceOf[Regex] -> IncludeRules( include.name )
//  implicit def symbols2tokens( tokens: Seq[Symbol] ) = tokens map (s => Token( s.name ))

  implicit def string2state( state: String ) = PushState( state )

  implicit def strings2states( states: Seq[String] ) = states map (s => PushState( s ))

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

//  implicit def string2regex( regex: String ) = regex.r

//  implicit def tuple2template( template: (String, String) ): (String, AST) = (template._1, template._2)

//  implicit def symbol2action( clas: Symbol ) = Action( clas )
}