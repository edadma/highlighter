package io.github.edadma.highlighter

val platform = "js"

// JS: RegExp doesn't support \p{Alpha} etc. Use ASCII ranges.
def translatePosixClass(cls: String): String = cls match
  case "[:alnum:]"  => "a-zA-Z0-9"
  case "[:alpha:]"  => "a-zA-Z"
  case "[:digit:]"  => "0-9"
  case "[:lower:]"  => "a-z"
  case "[:upper:]"  => "A-Z"
  case "[:space:]"  => "\\s"
  case "[:blank:]"  => " \\t"
  case "[:punct:]"  => "!-/:-@\\[-`{-~"
  case "[:xdigit:]" => "0-9a-fA-F"
  case "[:ascii:]"  => "\\x00-\\x7F"
  case other        => other
