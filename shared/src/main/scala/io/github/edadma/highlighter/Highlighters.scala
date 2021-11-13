package io.github.edadma.highlighter

import io.github.edadma.highlighter.highlighters.{
  BackslashHighlighter,
  CSSHighlighter,
  HTMLHighlighter,
  JSONHighlighter,
  JavascriptHighlighter,
  SquigglyHighlighter
}

import collection.mutable

object Highlighters {

  private val map =
    mutable.HashMap(
      List(
        BackslashHighlighter,
        CSSHighlighter,
        HTMLHighlighter,
        JavascriptHighlighter,
        JSONHighlighter,
        SquigglyHighlighter
      ) map (h => h.highlighterName.toLowerCase -> h): _*)

  def registered(h: String): Option[Highlighter] = map get h.toLowerCase

  def register(h: Highlighter): Unit = {
    require(!(map contains h.highlighterName.toLowerCase))
    map += (h.highlighterName.toLowerCase -> h)
  }

}
