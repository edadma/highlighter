package io.github.edadma.highlighter

val platform = "jvm"

// JVM: java.util.regex supports \p{Alpha} etc.
def translatePosixClass(cls: String): String = cls match
  case "[:alnum:]"  => "\\p{Alnum}"
  case "[:alpha:]"  => "\\p{Alpha}"
  case "[:digit:]"  => "\\p{Digit}"
  case "[:lower:]"  => "\\p{Lower}"
  case "[:upper:]"  => "\\p{Upper}"
  case "[:space:]"  => "\\p{Space}"
  case "[:blank:]"  => "\\p{Blank}"
  case "[:punct:]"  => "\\p{Punct}"
  case "[:xdigit:]" => "\\p{XDigit}"
  case "[:ascii:]"  => "\\p{ASCII}"
  case other        => other
