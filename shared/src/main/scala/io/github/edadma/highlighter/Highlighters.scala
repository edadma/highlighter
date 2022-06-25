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
      ) flatMap (h => (h.highlighterName -> h) +: h.highlighterAliases.map(_ -> h)): _*)

  def registered(h: String): Option[Highlighter] = map get h

  def register(h: Highlighter): Unit = {
    require(!(map.keySet.map(_.toLowerCase) contains h.highlighterName.toLowerCase))
    map += (h.highlighterName -> h)
    map ++= h.highlighterAliases.map(_ -> h)
  }

}
