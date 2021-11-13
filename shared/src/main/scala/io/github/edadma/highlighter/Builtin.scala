package io.github.edadma.highlighter


object Builtin {
  val map =
    Map(
      "Error" ->                  "err",
      "Other" ->                  "x",

      "Keyword" ->                "k",
      "Keyword.Constant" ->       "kc",
      "Keyword.Declaration" ->    "kd",
      "Keyword.Namespace" ->      "kn",
      "Keyword.Pseudo" ->         "kp",
      "Keyword.Reserved" ->       "kr",
      "Keyword.Type" ->           "kt",

      "Name" ->                   "n",
      "Name.Attribute" ->         "na",
      "Name.Builtin" ->           "nb",
      "Name.Builtin.Pseudo" ->    "bp",
      "Name.Class" ->             "nc",
      "Name.Constant" ->          "no",
      "Name.Decorator" ->         "nd",
      "Name.Entity" ->            "ni",
      "Name.Exception" ->         "ne",
      "Name.Function" ->          "nf",
      "Name.Function.Magic" ->    "fm",
      "Name.Property" ->          "py",
      "Name.Label" ->             "nl",
      "Name.Namespace" ->         "nn",
      "Name.Other" ->             "nx",
      "Name.Tag" ->               "nt",
      "Name.Variable" ->          "nv",
      "Name.Variable.Class" ->    "vc",
      "Name.Variable.Global" ->   "vg",
      "Name.Variable.Instance" -> "vi",
      "Name.Variable.Magic" ->    "vm",

      "Literal" ->                "l",
      "Literal.Date" ->           "ld",

      "String" ->                 "s",
      "String.Affix" ->           "sa",
      "String.Backtick" ->        "sb",
      "String.Char" ->            "sc",
      "String.Delimiter" ->       "dl",
      "String.Doc" ->             "sd",
      "String.Double" ->          "s2",
      "String.Escape" ->          "se",
      "String.Heredoc" ->         "sh",
      "String.Interpol" ->        "si",
      "String.Other" ->           "sx",
      "String.Regex" ->           "sr",
      "String.Single" ->          "s1",
      "String.Symbol" ->          "ss",

      "Number" ->                 "m",
      "Number.Bin" ->             "mb",
      "Number.Float" ->           "mf",
      "Number.Hex" ->             "mh",
      "Number.Integer" ->         "mi",
      "Number.Integer.Long" ->    "il",
      "Number.Oct" ->             "mo",

      "Operator" ->               "o",
      "Operator.Word" ->          "ow",

      "Punctuation" ->            "p",

      "Comment" ->                "c",
      "Comment.Hashbang" ->       "ch",
      "Comment.Multiline" ->      "cm",
      "Comment.Preproc" ->        "cp",
      "Comment.PreprocFile" ->    "cpf",
      "Comment.Single" ->         "c1",
      "Comment.Special" ->        "cs",

      "Generic" ->                "g",
      "Generic.Deleted" ->        "gd",
      "Generic.Emph" ->           "ge",
      "Generic.Error" ->          "gr",
      "Generic.Heading" ->        "gh",
      "Generic.Inserted" ->       "gi",
      "Generic.Output" ->         "go",
      "Generic.Prompt" ->         "gp",
      "Generic.Strong" ->         "gs",
      "Generic.Subheading" ->     "gu",
      "Generic.Traceback" ->      "gt"
  )
}