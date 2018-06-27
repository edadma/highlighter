package xyz.hyperreal.highlighter

import java.util.regex.Pattern


object PrintDefinition extends App {

  HighlighterParser( io.Source.stdin ) match {
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
          }
        case Templates( templates ) => _templates = templates
        case States( states ) => _states = states map (s => (s.name, s)) toMap
        case Includes( includes ) => _includes = includes
        case Classes( classes ) => _classes = classes
      }
  }

}