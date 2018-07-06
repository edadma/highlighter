//@
package xyz.hyperreal.highlighter

import xyz.hyperreal.highlighter.highlighters._


object Highlighters {

  val supported =
    List(
      BackslashHighlighter,
      CSSHighlighter,
      HTMLHighlighter,
      JavascriptHighlighter,
      JSONHighlighter
    ) map (h => h.highlighterName -> h) toMap

}
