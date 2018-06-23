package xyz.hyperreal.highlighter


object Main extends App {

  val input =
    """
      |val a = 123
    """.stripMargin
  val highlighter =
    new Highlighter {
      override def define: Definition =
        Definition(
          Seq(
            Templates(
              Map[String, String](
                "default" -> """<\class\ "\escape\text">"""
              )
            ),
            States(
              Seq(
                State( "root",
                  Seq(
                    MatchRule( "val", 'keyword )
                  )
                )
              )
            )
          )
        )
    }

}