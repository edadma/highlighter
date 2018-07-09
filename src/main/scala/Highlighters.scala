//@
package xyz.hyperreal.highlighter

import collection.mutable

import xyz.hyperreal.highlighter.highlighters._


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
