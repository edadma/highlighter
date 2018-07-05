//@
package xyz.hyperreal.highlighter

import java.util.regex.{MatchResult, Pattern}

import scala.collection.mutable.ArrayStack
import xyz.hyperreal.backslash.{AST, Command, Parser, Renderer}

import scala.collection.mutable


abstract class Highlighter {

  def define: Definition
  def config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )

  val dependencies = new mutable.HashMap[String, Highlighter]
  var trace = false
  val parser = new Parser( Command.standard )
  val (flags, highlighterName, templates, states, classes, includes, equates, named) = {
    var _flags = 0
    var _name: String = getClass.getName
    var _templates: Map[String, AST] = null
    var _states: Map[String, State] = null
    var _classes: Map[String, String] = null
    var _includes: Map[String, Seq[Rule]] = Map()
    var _equates: Map[String, RAST] = Map()
    var _named: Map[String, MatchRule] = Map()

    def addRules( rules: Seq[Rule] ) =
      rules foreach {
        case rule@MatchRule( Some(name), _, _ ) =>
          if (_named contains name)
            sys.error( s"duplicate rule name: $name" )

          _named += (name -> rule)
        case _ =>
      }

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
              case Unicode => _flags |= Pattern.UNICODE_CASE|Pattern.UNICODE_CHARACTER_CLASS
            }
          case Templates( templates ) => _templates = templates
          case States( states ) =>
            _states = states map (s => (s.name, s)) toMap

            states foreach {
              case State( _, rules ) => addRules( rules )
            }
          case Includes( includes ) =>
            _includes = includes

            includes foreach {
              case (_, rules) => addRules( rules )
            }
          case Classes( classes ) => _classes = classes
          case Equates( equates ) => _equates = equates
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

    (_flags, _name, _templates, _states, if (_classes eq null) Builtin.map else _classes, _includes, _equates, _named) }
  val renderer = new Renderer( parser, config )

  def eval( rast: RAST ): Any =
    rast match {
      case SeqRAST( seq ) => seq map eval mkString
      case StaticRAST( s ) => s
      case ListRAST( l ) => l map eval
      case LiteralRAST( v ) => v
      case MutableRAST( expr ) => eval( expr )
      case VariableRAST( v ) => eval( equates(v) )
      case FunctionRAST( "words", List(words: RAST) ) =>
        eval( words ).asInstanceOf[List[_]] map (_.toString) sortWith (_.length > _.length) map Pattern.quote mkString ("(?:", "|", ")")
      case FunctionRAST( f, args ) => sys.error( s"unknown function: $f, $args" )
      case BinaryRAST( l, "+", r ) =>
        (eval( l ), eval( r )) match {
          case (a: List[_], b: List[_]) => a ++ b
          case (a, b) => a.toString + b.toString
        }
    }

  def highlight( code: io.Source ): String = highlight( code mkString )

  def highlight( code: String, send: (String, Chars) => Unit = null ): String = {

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

    def output( s: String, clas: Chars ) {
      def out( cls: String, tmp: String ): Unit = {
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

      if (send ne null)
        send( s, clas )
      else if (s nonEmpty) {
        dotrace( "output",  s""""$s", $clas""" )

        clas match {
          case Token( c@("text"|"Text"), _ ) => out( c, "text" )
          case Token( c, t ) => out( c, t )
          case u@Using( dependency ) => u.highlighter(
            dependencies get dependency match {
              case None => sys.error( s"dependency not found: $dependency" )
              case Some( highlighter ) => highlighter
            } ).highlight( s, output )
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
          case rule@MatchRule( _, regex, actions ) =>
            prefix( rule.pattern(Pattern.compile(eval(regex).toString.trim, flags)) ) map (m => (m, actions))
          case rule@MismatchRule( regex, actions ) =>
            prefix( rule.pattern(Pattern.compile(eval(regex).toString.trim, flags)) ) map (_ => (null, actions))
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
              case Groups( toks ) =>
                if (toks.length != info.groupCount)
                  sys.error( "number of groups not equal to number of tokens" ) // put a Position in the Rule
                for ((t, i) <- toks zipWithIndex)
                  output( code.substring(info.start(i + 1), info.end(i + 1)), t )
              case action@Push( statename ) =>
                stack push action.state(
                  states get statename match {
                    case None => sys.error( s"unknown state: $statename" )
                    case Some( s ) => s
                  } )
              case Pop => stack pop
              case Popn( n ) => for (_ <- 1 to n) stack pop
              case Alter( name, regex ) =>
                named get name match {
                  case None => sys.error( s"rule not found: $name" )
                  case Some( rule ) =>
                    rule.pattern.unset
                    rule.regex = regex
                }
            }

            highlight( if (info eq null) pos else info.end )
        }
    }

    highlight( 0 )
    flush

    if (trace)
      ""
    else {
      templates get "layout" match {
        case None => result.toString
        case Some( t ) => renderer.capture( t, Map("content" -> result.toString) )
      }
    }
  }

}