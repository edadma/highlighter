package io.github.edadma.highlighter

import scala.collection.mutable.ListBuffer
import pprint._

import scala.language.postfixOps

object Main extends App {

  if (args.isEmpty) {
    println("java -jar highlighter.jar <definition>... [<input>]")
    sys.exit(1)
  }

  val definition = HighlighterParser(scala.io.Source.fromFile(args(0)))
  val highlighter =
    new Highlighter {
      for (i <- 1 to args.length - 2) {
        val dep =
          new Highlighter {
            def define: Definition = HighlighterParser(scala.io.Source.fromFile(args(i)))
          }

        Highlighters.register(dep)
      }

      def define: Definition = definition
    }

  if (args.length == 1) {
    val deps = new ListBuffer[String]

    (highlighter.includes.values.flatten ++ highlighter.states.values.flatMap { case State(_, rules) => rules }) flatMap (_.actions) foreach {
      case Match(Using(name)) => deps += name
      case Groups(toks) =>
        toks foreach {
          case Using(name) => deps += name
          case _           =>
        }
      case Output(_, Using(name)) => deps += name
      case _                      =>
    }

    println(
      """package io.github.edadma.highlighter.highlighters
        |
        |import io.github.edadma.backslash._
        |import io.github.edadma.highlighter._
        |
        |import scala.util.parsing.input.OffsetPosition
        |
        |""".stripMargin
    )
    println(s"object ${highlighter.highlighterName}Highlighter extends Highlighter {\n")
    println("  def define =")
    pprintln(definition)
    println("\n}")
  } else
    println(highlighter.highlight(scala.io.Source.fromFile(args.last)))

}
