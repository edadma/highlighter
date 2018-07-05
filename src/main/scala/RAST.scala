package xyz.hyperreal.highlighter


trait RAST
case class StaticRAST( s: String ) extends RAST
case class LiteralRAST( v: Any ) extends RAST
case class FunctionRAST( f: String, args: List[RAST] ) extends RAST
case class ListRAST( l: List[RAST] ) extends RAST
case class VariableRAST( v: String ) extends RAST
case class BinaryRAST( l: RAST, op: String, r: RAST ) extends RAST
case class MutableRAST( var expr: RAST ) extends RAST