//@
package io.github.edadma.highlighter

import io.github.edadma.highlighter.highlighters.{BackslashHighlighter, CSSHighlighter, HTMLHighlighter, JSONHighlighter, JavascriptHighlighter}

import collection.mutable


object Highlighters {

  private val map =
    mutable.HashMap( List(
      BackslashHighlighter,
      CSSHighlighter,
      HTMLHighlighter,
      JavascriptHighlighter,
      JSONHighlighter
    ) map (h => h.highlighterName.toLowerCase -> h): _* )

  def registered( h: String ) = map get h.toLowerCase

  def register( h: Highlighter ) = map += (h.highlighterName.toLowerCase -> h)

}
