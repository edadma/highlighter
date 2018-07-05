package xyz.hyperreal.highlighter.highlighters

import xyz.hyperreal.backslash._
import xyz.hyperreal.highlighter._

import scala.util.parsing.input.OffsetPosition


object HTMLHighlighter extends Highlighter {

  dependencies += ("CSS" -> CSSHighlighter)

  def define = null

}