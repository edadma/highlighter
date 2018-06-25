//@
package xyz.hyperreal.highlighter

import java.util.regex.{MatchResult, Pattern}

import scala.util.matching.Regex
import scala.collection.mutable.ArrayStack
import xyz.hyperreal.backslash.{AST, Command, Parser, Renderer}


abstract class Highlighter {

  def define: Definition

  var trace = false
  val parser = new Parser( Command.standard )
  val (flags, name, templates, states, classes, includes) = {
    var _flags = 0
    var _name: String = getClass.getName
    var _templates: Map[String, AST] = null
    var _states: Map[String, State] = null
    var _classes: Map[String, String] = Map()
    var _includes: Map[String, Seq[Rule]] = Map()

    define match {
      case Definition( sections ) =>
        sections foreach {
          case InfoItems( items ) =>
            items foreach {
              case Name( s ) => _name = s
            }
          case Options( options ) =>
            options foreach {
              case Ignorecase => _flags |= Pattern.CASE_INSENSITIVE
              case Dotall => _flags |= Pattern.DOTALL
              case Multiline => _flags |= Pattern.MULTILINE
            }
          case Templates( templates ) => _templates = templates
          case States( states ) => _states = states map (s => (s.name, s)) toMap
          case Includes( includes ) => _includes = includes
          case Classes( classes ) => _classes = classes
        }
    }

    if (_templates eq null)
      sys.error( "missing template section" )

    if (!_templates.contains( "default" ))
      sys.error( "missing default template" )

    if (_states eq null)
      sys.error( "missing states section" )

    if (!_states.contains( "root" ))
      sys.error( "missing root state" )

    (_flags, _name, _templates, _states, _classes, _includes) }
  val config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )
  val renderer = new Renderer( parser, config )

  def highlight( code: String ) = {

    val stack =
      new ArrayStack[State] {
        push( states("root") )
      }
    val result = new StringBuilder
    val chunk = new StringBuilder
    var prevclass: String = null
    var prevast: AST = null

    def dotrace( s: Any ) =
      if (trace)
        println( s )

    def output( s: String, clas: Token ) =
      if (s nonEmpty) {
        dotrace( "output",  s""""$s", $clas""" )

        val (cls, tmp) =
          clas match {
            case Token( c, t ) => (c, t)
          }

        templates get tmp match {
          case None =>
            if (tmp == "text") {
              flush
              result ++= s
            } else
              sys.error( s"unrecognized template: $tmp" )
          case Some( ast ) =>
            if (cls == prevclass)
              chunk ++= s
            else {
              flush
              prevast = ast
              prevclass = cls
              chunk ++= s
            }
        }
      }

    def flush: Unit =
      if (prevclass ne null) {
        result ++=
          renderer.capture( prevast, Map("class" -> classes.getOrElse(prevclass, prevclass), "text" -> chunk.toString) )
        chunk.clear
        prevclass = null
      }

    def text( s: String ): Unit = output( s, "text/text" )

    def highlight( pos: Int ): Unit = {
      def prefix( regex: Pattern ) = {
        val m = regex matcher code

        m.region( pos, code.length )
        if (m.lookingAt) Some( m ) else None
      }

      def search[A, B]( seq: Seq[A], f: A => Option[B] ): Option[B] = {
        val it = seq.iterator

        while (it.hasNext)
          f( it.next ) match {
            case None =>
            case res => return res
          }

        None
      }

      def apply( rule: Rule ): Option[(MatchResult, Seq[Action])] =
        rule match {
          case rule@MatchRule( regex, actions ) =>
            prefix( rule.pattern(Pattern.compile(regex, flags)) ) map (m => (m, actions))
          case rule@MismatchRule( regex, actions ) =>
            prefix( rule.pattern(Pattern.compile(regex, flags)) ) map (_ => (null, actions))
          case DefaultRule( actions ) => Some( (null, actions) )
          case rule@IncludeRule( include ) =>
            search( rule.rules(
              includes get include match {
                case None => sys.error( s"unknown include: $include" )
                case Some( r ) => r
              } ), apply )
        }

      dotrace( s"search: pos = $pos" )

      if (pos < code.length)
        search( stack.top.rules, apply ) match {
          case None =>
            text( code charAt pos toString )
            highlight( pos + 1 )
          case Some( (info, actions) ) =>
            actions foreach {
              case Match( tok ) =>
                output( code.substring(info.start, info.end), tok )
              case action@Push( name ) =>
                stack push action.state(
                  states get name match {
                    case None => sys.error( s"unknown state: $name" )
                    case Some( s ) => s
                  } )
              case Pop => stack pop
            }

            highlight( if (info eq null) pos else info.end )
        }
    }

    highlight( 0 )
    flush

    if (trace) "" else result.toString
  }

}