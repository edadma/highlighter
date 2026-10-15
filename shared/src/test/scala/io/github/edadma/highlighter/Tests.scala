package io.github.edadma.highlighter

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  val simpleGrammar = """{
    "scopeName": "source.test",
    "patterns": [
      { "match": "\\b(if|else|val|def)\\b", "name": "keyword.control.test" },
      { "match": "\\b\\d+\\b", "name": "constant.numeric.test" },
      { "begin": "\"", "end": "\"", "name": "string.quoted.double.test",
        "patterns": [{ "match": "\\\\.", "name": "constant.character.escape.test" }] },
      { "match": "//.*$", "name": "comment.line.test" }
    ]
  }"""

  "tokenizer" - {
    "keywords" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("val x = 42")
      result should include("keyword")
      result should include("number")
    }

    "strings" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("""val s = "hello"""")
      result should include("string")
    }

    "comments" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("val x = 1 // note")
      result should include("comment")
    }

    "multiline begin/end spans lines" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("\"hello\nworld\"")
      result should include("string")
    }
  }

  "render modes" - {
    "class mode default prefix" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("val x")
      result should include("""class="hl-keyword"""")
    }

    "class mode custom prefix" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("code-")): @unchecked
      val result = hl.highlight("val x")
      result should include("""class="code-keyword"""")
    }

    "inline mode OneDark" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, InlineMode(Theme.OneDark)): @unchecked
      val result = hl.highlight("val x")
      result should include("""style="color:#c678dd"""")
    }

    "inline mode OneLight" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, InlineMode(Theme.OneLight)): @unchecked
      val result = hl.highlight("val x")
      result should include("""style="color:#a626a4"""")
    }
  }

  "structured tokens" - {
    "tokens are grouped one list per source line and reproduce the line" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val lines     = hl.tokens("val x = 1\n// note")
      lines.length shouldBe 2
      lines.head.map(_.text).mkString shouldBe "val x = 1"
      lines(1).map(_.text).mkString shouldBe "// note"
    }

    "each token carries a category a caller can map to its own palette" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val toks      = hl.tokens("val x = 42").head
      hl.categoryOf(toks.find(_.text == "val").get) shouldBe "keyword"
      hl.categoryOf(toks.find(_.text == "42").get) shouldBe "number"
      hl.category("comment.line.test") shouldBe "comment"
    }
  }

  // Delimiters scoped the way real grammars scope them: the quotes and the comment marker are
  // `punctuation.definition.…` inside the string or comment they delimit.
  val delimitedGrammar = """{
    "scopeName": "source.delim",
    "patterns": [
      { "begin": "\"", "end": "\"", "name": "string.quoted.double.delim",
        "beginCaptures": { "0": { "name": "punctuation.definition.string.begin.delim" } },
        "endCaptures": { "0": { "name": "punctuation.definition.string.end.delim" } },
        "patterns": [
          { "match": ",", "name": "punctuation.separator.delim" },
          { "match": "@", "name": "meta.at.delim" }
        ] },
      { "match": "(//)(.*)$", "name": "comment.line.delim",
        "captures": { "1": { "name": "punctuation.definition.comment.delim" } } },
      { "match": "\\[", "name": "punctuation.definition.bracket.delim" }
    ]
  }"""

  "the category is read from the whole scope stack" - {
    def categories(code: String): List[(String, String)] =
      val Right(hl) = Highlighter.fromJson(delimitedGrammar): @unchecked
      hl.tokens(code).flatten.map(t => (t.text, hl.categoryOf(t)))

    "a string's quotes take the string's category" in {
      categories("\"hi\"") shouldBe List(("\"", "string"), ("hi", "string"), ("\"", "string"))
    }

    "a comment's marker takes the comment's category" in {
      categories("// note").map(_._2).distinct shouldBe List("comment")
    }

    "a definition with no categorised enclosing scope is still punctuation" in {
      categories("[") shouldBe List(("[", "punctuation"))
    }

    "a definition nested in another definition still reaches the construct around both" in {
      val Right(hl) = Highlighter.fromJson(delimitedGrammar): @unchecked
      val nested =
        Token("<", List("source.delim", "string.quoted.double.delim", "punctuation.definition.tag.delim",
          "punctuation.definition.string.begin.delim"))
      hl.categoryOf(nested) shouldBe "string"
    }

    "other punctuation inside a string keeps its own category" in {
      categories("\"a,b\"") should contain((",", "punctuation"))
    }

    "an uncategorised inner scope inside a string is string" in {
      categories("\"a@b\"") should contain(("@", "string"))
    }

    "rendered HTML agrees with categoryOf" in {
      val Right(hl) = Highlighter.fromJson(delimitedGrammar, ClassMode("hl-")): @unchecked
      hl.highlight("\"hi\"") shouldBe """<span class="hl-string">&quot;</span><span class="hl-string">hi</span><span class="hl-string">&quot;</span>"""
      val Right(inline) = Highlighter.fromJson(delimitedGrammar, InlineMode(Theme.OneLight)): @unchecked
      inline.highlight("// x") should not include s"color:${Theme.OneLight.punctuation}"
    }
  }

  "html escaping" - {
    "escapes angle brackets" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("val x = <tag>")
      result should include("&lt;tag&gt;")
    }

    "escapes ampersands" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("val x = a & b")
      result should include("&amp;")
    }
  }

  "repository includes" - {
    val grammarWithRepo = """{
      "scopeName": "source.repo",
      "patterns": [
        { "include": "#keywords" },
        { "include": "#literals" }
      ],
      "repository": {
        "keywords": {
          "patterns": [
            { "match": "\\b(let|const|function|return)\\b", "name": "keyword.control.repo" }
          ]
        },
        "literals": {
          "patterns": [
            { "include": "#numbers" },
            { "include": "#strings" }
          ]
        },
        "numbers": {
          "match": "\\b\\d+\\b",
          "name": "constant.numeric.repo"
        },
        "strings": {
          "begin": "\"",
          "end": "\"",
          "name": "string.quoted.double.repo"
        }
      }
    }"""

    "resolves top-level includes" in {
      val Right(hl) = Highlighter.fromJson(grammarWithRepo): @unchecked
      val result = hl.highlight("let x = 42")
      result should include("keyword")
      result should include("number")
    }

    "resolves nested includes (two levels deep)" in {
      val Right(hl) = Highlighter.fromJson(grammarWithRepo): @unchecked
      val result = hl.highlight("""let s = "hi"""")
      result should include("string")
    }

    "resolves single-pattern repository entries" in {
      val Right(hl) = Highlighter.fromJson(grammarWithRepo): @unchecked
      val result = hl.highlight("return 99")
      result should include("keyword")
      result should include("number")
    }
  }

  "begin/end with captures" - {
    val grammarWithCaptures = """{
      "scopeName": "source.cap",
      "patterns": [
        {
          "name": "meta.function.cap",
          "begin": "(function)\\s+([a-zA-Z_]\\w*)",
          "beginCaptures": {
            "1": { "name": "storage.type.function.cap" },
            "2": { "name": "entity.name.function.cap" }
          },
          "end": "\\}",
          "patterns": [
            { "match": "\\b(return)\\b", "name": "keyword.control.cap" },
            { "match": "\\b\\d+\\b", "name": "constant.numeric.cap" }
          ]
        },
        { "match": "\\b\\d+\\b", "name": "constant.numeric.cap" }
      ]
    }"""

    "emits capture group scopes" in {
      val Right(hl) = Highlighter.fromJson(grammarWithCaptures, ClassMode("hl-")): @unchecked
      val result = hl.highlight("function foo { return 1 }")
      result should include("hl-keyword") // storage.type → keyword
      result should include("hl-function") // entity.name.function → function
    }

    "inner patterns work inside begin/end" in {
      val Right(hl) = Highlighter.fromJson(grammarWithCaptures, ClassMode("hl-")): @unchecked
      val result = hl.highlight("function foo { return 42 }")
      result should include("hl-number") // constant.numeric inside region
    }
  }

  "cycle detection" - {
    val cyclicGrammar = """{
      "scopeName": "source.cycle",
      "patterns": [
        { "include": "#a" }
      ],
      "repository": {
        "a": {
          "patterns": [
            { "include": "#b" },
            { "match": "\\b(hello)\\b", "name": "keyword.control.cycle" }
          ]
        },
        "b": {
          "patterns": [
            { "include": "#a" },
            { "match": "\\b\\d+\\b", "name": "constant.numeric.cycle" }
          ]
        }
      }
    }"""

    "handles mutually recursive includes without stack overflow" in {
      val Right(hl) = Highlighter.fromJson(cyclicGrammar): @unchecked
      val result = hl.highlight("hello 42")
      // Should not throw StackOverflowError
      result should include("keyword")
    }
  }

  "$self includes" - {
    val selfGrammar = """{
      "scopeName": "source.self",
      "patterns": [
        { "match": "\\b(if|else)\\b", "name": "keyword.control.self" },
        { "begin": "\\{", "end": "\\}", "name": "meta.block.self",
          "patterns": [{ "include": "$self" }]
        },
        { "match": "\\b\\d+\\b", "name": "constant.numeric.self" }
      ]
    }"""

    "re-applies grammar inside nested blocks" in {
      val Right(hl) = Highlighter.fromJson(selfGrammar): @unchecked
      val result = hl.highlight("if { 42 }")
      result should include("keyword")
      result should include("number")
    }
  }

  "token merging" - {
    "merges adjacent tokens with same scope" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight(""""hello"""")
      // The string delimiters and content should be merged into one span
      result should not include """hl-string">h</span><span class="hl-string"""
    }
  }

  "unknown fields in grammar JSON" - {
    "ignores extra top-level fields" in {
      val grammarWithExtras = """{
        "scopeName": "source.extra",
        "name": "Extra Test",
        "version": "1.0.0",
        "information_for_contributors": ["test"],
        "patterns": [
          { "match": "\\b(foo)\\b", "name": "keyword.control.extra" }
        ]
      }"""
      val result = Highlighter.fromJson(grammarWithExtras)
      result.isRight shouldBe true
    }
  }

  "nested capture groups" - {
    // A capture group wrapping another (`((:))`) makes two groups cover
    // the same characters — the shape XML namespace patterns use for the
    // `:` separator. The tokenizer must emit that span ONCE (layering the
    // inner group's scope on top), not once per covering group, which
    // would duplicate the character in the output.
    val nestedGrammar = """{
      "scopeName": "source.nested",
      "patterns": [
        { "match": "([a-z]+)((:))([a-z]+)",
          "captures": {
            "1": { "name": "entity.name.tag.ns.nested" },
            "2": { "name": "entity.name.tag.nested" },
            "3": { "name": "punctuation.separator.nested" },
            "4": { "name": "entity.name.tag.local.nested" }
          }
        }
      ]
    }"""

    def visibleText(html: String): String =
      html.replaceAll("<span[^>]*>", "").replaceAll("</span>", "")

    "does not duplicate text covered by nested groups" in {
      val Right(hl) = Highlighter.fromJson(nestedGrammar, ClassMode("hl-")): @unchecked
      val out = hl.highlight("foo:bar")
      visibleText(out) shouldBe "foo:bar" // exactly one colon, no duplication
    }

    "innermost nested group wins the scope" in {
      val Right(hl) = Highlighter.fromJson(nestedGrammar, ClassMode("hl-")): @unchecked
      val out = hl.highlight("foo:bar")
      // The colon is covered by group 2 (entity → function) and the
      // inner group 3 (punctuation); the inner, higher-numbered group
      // layers last, so the colon renders as punctuation.
      out should include("""<span class="hl-punctuation">:</span>""")
    }
  }

  "edge cases" - {
    "empty input" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      hl.highlight("") shouldBe ""
    }

    "input with only whitespace" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      hl.highlight("   ") shouldBe "   "
    }

    "no patterns match" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("xyz abc")
      result shouldBe "xyz abc"
    }

    "multiple keywords on one line" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("if x else val")
      result.split("hl-keyword").length shouldBe 4 // 3 keywords = 4 parts
    }

    "adjacent patterns with no gap" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("42//comment")
      result should include("hl-number")
      result should include("hl-comment")
    }

    "begin/end with no content" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("\"\"")
      result should include("hl-string")
    }

    "unclosed begin/end spans to end of input" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("\"hello")
      result should include("hl-string")
    }

    "special characters in unmatched text" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar): @unchecked
      val result = hl.highlight("<div>&</div>")
      result should include("&lt;")
      result should include("&amp;")
      result should include("&gt;")
    }

    "multiline with blank lines" in {
      val Right(hl) = Highlighter.fromJson(simpleGrammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("val x\n\nval y")
      result.split("\n").length shouldBe 3
    }

    "zero-length match does not loop" in {
      val grammar = """{
        "scopeName": "source.zero",
        "patterns": [
          { "match": "(?=x)", "name": "meta.lookahead" },
          { "match": ".", "name": "keyword.other" }
        ]
      }"""
      val Right(hl) = Highlighter.fromJson(grammar): @unchecked
      // Should complete without infinite loop
      val result = hl.highlight("xyz")
      result.length should be > 0
    }

    "POSIX character classes in patterns" in {
      val grammar = """{
        "scopeName": "source.posix",
        "patterns": [
          { "match": "[[:alpha:]]+", "name": "keyword.control.posix" },
          { "match": "[[:digit:]]+", "name": "constant.numeric.posix" }
        ]
      }"""
      val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("hello 42")
      result should include("hl-keyword")
      result should include("hl-number")
    }

    "invalid regex in pattern is silently skipped" in {
      val grammar = """{
        "scopeName": "source.badrx",
        "patterns": [
          { "match": "[invalid", "name": "keyword.bad" },
          { "match": "\\b(good)\\b", "name": "keyword.control.good" }
        ]
      }"""
      val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("good stuff")
      result should include("hl-keyword")
    }

    "deeply nested $self does not stack overflow" in {
      val grammar = """{
        "scopeName": "source.deep",
        "patterns": [
          { "match": "\\b(kw)\\b", "name": "keyword.control.deep" },
          { "begin": "\\(", "end": "\\)", "name": "meta.parens.deep",
            "patterns": [{ "include": "$self" }] }
        ]
      }"""
      val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("(kw (kw (kw)))")
      result.split("hl-keyword").length should be >= 4 // at least 3 kw matches
    }

    "contentName adds scope to inner content" in {
      val grammar = """{
        "scopeName": "source.cn",
        "patterns": [
          { "begin": "\\{", "end": "\\}", "name": "meta.block.cn",
            "contentName": "string.content.cn",
            "patterns": [] }
        ]
      }"""
      val Right(hl) = Highlighter.fromJson(grammar, ClassMode("hl-")): @unchecked
      val result = hl.highlight("{ hello }")
      result should include("hl-string")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // Ported from the sysl highlighter's own suite. Each name is the sysl test's name.
  // ─────────────────────────────────────────────────────────────────

  private def classHl(json: String): Highlighter =
    Highlighter.fromJson(json, ClassMode("hl-")) match
      case Right(h)  => h
      case Left(err) => fail(s"the grammar should load: $err")

  private def scopesOfToken(h: Highlighter, code: String, text: String): String =
    h.tokens(code).flatten.find(_.text == text).getOrElse(fail(s"no token reads '$text'")).scopes.mkString(" ")

  private val keywordGrammar = """{
    "scopeName": "source.kw",
    "patterns": [
      { "match": "\\b(if|else|real)\\b", "name": "keyword.control.kw" },
      { "match": "\\w+", "name": "variable.other.kw" }
    ]
  }"""

  // The oniguruma engine reads `\w` as ASCII (Ruby's rule) where vscode-oniguruma reads it as Unicode, so `\w+` stops
  // at an accented letter and a keyword rule finds a boundary inside a word. The fix is the engine's option, not this
  // library's; these are ignored until `oniguruma` offers it.
  "\\b is Unicode: a keyword inside a word is not a keyword" - {
    "\\b: 'if' at the end of 'síif' is not a keyword" ignore {
      val out = classHl(keywordGrammar).highlight("síif")
      out should not include "hl-keyword"
      out shouldBe """<span class="hl-variable">síif</span>"""
    }

    "\\b: 'real' at the start of 'realíssimo' is not a keyword" ignore {
      val out = classHl(keywordGrammar).highlight("realíssimo")
      out should not include "hl-keyword"
      out shouldBe """<span class="hl-variable">realíssimo</span>"""
    }

    "\\b: a keyword standing alone beside a non-ASCII word still is one" ignore {
      classHl(keywordGrammar).highlight("if síif") shouldBe
        """<span class="hl-keyword">if</span> <span class="hl-variable">síif</span>"""
    }

    "\\w is Unicode, as in VS Code: one \\w+ takes 'naïve' whole" ignore {
      classHl(keywordGrammar).highlight("naïve") shouldBe """<span class="hl-variable">naïve</span>"""
    }
  }

  "an end naming its begin's groups" - {
    val heredoc = """{
      "scopeName": "source.doc",
      "patterns": [
        { "begin": "<<(\\w+)", "end": "^\\1$", "name": "string.unquoted.heredoc.doc",
          "beginCaptures": { "1": { "name": "keyword.control.heredoc-token.doc" } },
          "endCaptures": { "0": { "name": "keyword.control.heredoc-token.doc" } } },
        { "match": "\\b(echo)\\b", "name": "support.function.doc" }
      ]
    }"""

    "an end naming its begin's group closes on that group's text" in {
      val out = classHl(heredoc).highlight("cat <<EOF\necho inside\nEOF\necho after")
      out should include("""<span class="hl-string">echo inside</span>""")
      out should include("""<span class="hl-function">echo</span> after""")
    }

    "an end naming its begin's group does not close on other text" in {
      val out = classHl(heredoc).highlight("cat <<EOF\nEND\necho still")
      out should include("""<span class="hl-string">echo still</span>""")
    }

    "two pairs open at once each close on their own text" in {
      val h = classHl(heredoc)
      h.highlight("<<A\nB\nA\necho x") should include("""<span class="hl-function">echo</span>""")
      h.highlight("<<B\nA\nB\necho x") should include("""<span class="hl-function">echo</span>""")
    }

    "a begin group's text is escaped before it stands in an end" in {
      val g = """{
        "scopeName": "source.esc",
        "patterns": [
          { "begin": "\\[(\\S+)\\[", "end": "\\]\\1\\]", "name": "string.quoted.esc" }
        ]
      }"""
      // `a.b` must close on `a.b` and not on `axb`, which an unescaped `.` would take.
      classHl(g).highlight("[a.b[ x ]axb] y ]a.b] z") shouldBe
        """<span class="hl-string">[a.b[ x ]axb] y ]a.b]</span> z"""
    }

    "a group that took no part stands in an end as nothing" in {
      val g = """{
        "scopeName": "source.opt",
        "patterns": [
          { "begin": "<(x)?", "end": ">\\1", "name": "string.quoted.opt" }
        ]
      }"""
      classHl(g).highlight("<a> b") shouldBe """<span class="hl-string">&lt;a&gt;</span> b"""
    }
  }

  "character boundaries" - {
    "a rule that matches nothing where the line stands ends the line's matching, as in VS Code" in {
      val g = """{
        "scopeName": "source.zw",
        "patterns": [ { "match": "(?=é)", "name": "meta.zw" }, { "match": "é", "name": "keyword.zw" } ]
      }"""
      val h = classHl(g)
      h.highlight("aéé") shouldBe "aéé"
      h.highlight("é\né") shouldBe "é\né"
      h.tokens("aéé").head.length shouldBe 1
      h.tokens("aéé").head.head.text shouldBe "aéé"
    }

    "a zero-length match at the end of a line ends the line" in {
      val g = """{
        "scopeName": "source.eol",
        "patterns": [ { "match": "$", "name": "meta.eol" } ]
      }"""
      classHl(g).highlight("abc\ndef") shouldBe "abc\ndef"
    }
  }

  "what does not load" - {
    "text that is not JSON is refused, with the reader's diagnostic" in {
      Highlighter.fromJson("{ \"scopeName\": ").isLeft shouldBe true
    }

    "a grammar with no scopeName is refused" in {
      val r = Highlighter.fromJson("{ \"patterns\": [] }")
      r.isLeft shouldBe true
      r.left.getOrElse("") should include("scopeName")
    }

    "a rule key holding the wrong kind of value is refused, naming it" in {
      val r = Highlighter.fromJson("{ \"scopeName\": \"s\", \"patterns\": [ { \"match\": 3 } ] }")
      r.isLeft shouldBe true
      r.left.getOrElse("") should include("match")
    }

    "a JSON value that is not an object is refused" in {
      Highlighter.fromJson("[]").isLeft shouldBe true
    }

    "an include of a repository entry that does not exist resolves to nothing" in {
      classHl("{ \"scopeName\": \"s\", \"patterns\": [ { \"include\": \"#nowhere\" } ] }").highlight("x") shouldBe "x"
    }
  }

  "the renderer" - {
    "inline rendering writes the theme's colour for each category" in {
      val g = "{ \"scopeName\": \"s\", \"patterns\": [ { \"match\": \"\\\\d+\", \"name\": \"constant.numeric.s\" } ] }"
      val Right(h) = Highlighter.fromJson(g, InlineMode(Theme.Monokai)): @unchecked
      h.highlight("a 42") shouldBe """a <span style="color:#ae81ff">42</span>"""
    }

    "category: every row of the table" in {
      val h = classHl(simpleGrammar)
      h.category("keyword.control.x") shouldBe "keyword"
      h.category("storage.type.x") shouldBe "keyword"
      h.category("string.quoted.x") shouldBe "string"
      h.category("comment.line.x") shouldBe "comment"
      h.category("constant.numeric.x") shouldBe "number"
      h.category("constant.language.x") shouldBe "variable"
      h.category("entity.name.type.x") shouldBe "type"
      h.category("entity.name.function.x") shouldBe "function"
      h.category("entity.other.attribute-name.x") shouldBe "function"
      h.category("variable.other.x") shouldBe "variable"
      h.category("support.type.x") shouldBe "type"
      h.category("support.function.x") shouldBe "function"
      h.category("support.other.x") shouldBe ""
      h.category("punctuation.x") shouldBe "punctuation"
      h.category("meta.block.x") shouldBe ""
      h.category("source.x") shouldBe ""
      h.category("") shouldBe ""
    }

    "escape_html escapes the four and nothing else" in {
      val h = classHl("""{ "scopeName": "source.none", "patterns": [] }""")
      h.highlight("a<b>&\"c'") shouldBe "a&lt;b&gt;&amp;&quot;c'"
      h.highlight("plain") shouldBe "plain"
    }
  }

  "a line is matched with its newline" - {
    "newline: a rule ending on \\n closes at the end of its line" in {
      val g = """{
        "scopeName": "source.nl",
        "patterns": [
          { "begin": "#", "end": "\\n", "name": "comment.line.nl" },
          { "match": "\\bkey\\b", "name": "keyword.nl" }
        ]
      }"""
      classHl(g).highlight("# note\nkey") shouldBe
        """<span class="hl-comment"># note</span>""" + "\n" + """<span class="hl-keyword">key</span>"""
    }
  }

  "\\G holds where the last begin ended" - {
    val anchored = """{
      "scopeName": "source.g",
      "patterns": [
        { "begin": "<", "end": "(?!\\G)", "name": "meta.tag.g",
          "patterns": [ { "match": "\\G\\w+", "name": "keyword.g" } ] },
        { "match": "\\G\\w+", "name": "string.g" }
      ]
    }"""

    "\\G: holds where the begin match ended" in {
      classHl(anchored).highlight("<ab cd") shouldBe """&lt;<span class="hl-keyword">ab</span> cd"""
    }

    "\\G: holds nowhere at the start of a line" in {
      classHl(anchored).highlight("ab\ncd") shouldBe "ab\ncd"
    }

    "\\G: a begin match running to the end of its line makes it hold at each later line's start" in {
      val g = """{
        "scopeName": "source.eol",
        "patterns": [
          { "begin": "=\\n", "end": "^;", "name": "meta.block.eol",
            "patterns": [ { "match": "\\G\\w+", "name": "keyword.eol" } ] }
        ]
      }"""
      classHl(g).highlight("=\nab cd\nef\n;") shouldBe
        "=\n" + """<span class="hl-keyword">ab</span>""" + " cd\n" + """<span class="hl-keyword">ef</span>""" + "\n;"
    }
  }

  "\\A holds at the document's start and nowhere else" - {
    val shebang =
      """{ "scopeName": "source.sa", "patterns": [ { "match": "\\A#!.*", "name": "comment.line.shebang.sa" } ] }"""

    "\\A: holds at the start of the document" in {
      classHl(shebang).highlight("#!x\n#!y") shouldBe """<span class="hl-comment">#!x</span>""" + "\n#!y"
    }

    "\\A: holds at no other line's start" in {
      classHl(shebang).highlight("x\n#!y") shouldBe "x\n#!y"
    }
  }

  "a name may hold several scopes, and $N" - {
    "names: $N stands for the group's text" in {
      val g = """{
        "scopeName": "source.n",
        "patterns": [ { "match": "(\\w+)!", "name": "keyword.$1.n" }, { "match": "@(\\w+)", "name": "entity.${1:/upcase}.n" } ]
      }"""
      val h = classHl(g)
      scopesOfToken(h, "go!", "go!") shouldBe "source.n keyword.go.n"
      scopesOfToken(h, "@ab", "@ab") shouldBe "source.n entity.AB.n"
    }
  }

  "captures" - {
    "captures: a begin/end rule's captures apply to its end as well" in {
      val g = """{
        "scopeName": "source.c",
        "patterns": [ { "begin": "<", "end": ">", "captures": { "0": { "name": "punctuation.c" } } } ]
      }"""
      classHl(g).highlight("<a>") shouldBe
        """<span class="hl-punctuation">&lt;</span>a<span class="hl-punctuation">&gt;</span>"""
    }

    "captures: a capture's patterns tokenize its text again" in {
      val g = """{
        "scopeName": "source.r",
        "patterns": [
          { "match": "(\\w+)\\(",
            "captures": { "1": { "name": "entity.name.function.r", "patterns": [ { "match": "get", "name": "keyword.r" } ] } } }
        ]
      }"""
      val h = classHl(g)
      h.highlight("getx(") shouldBe """<span class="hl-keyword">get</span><span class="hl-function">x</span>("""
      scopesOfToken(h, "getx(", "get") shouldBe "source.r entity.name.function.r keyword.r"
    }

    "captures: group 0's scope stays beneath the groups inside it" in {
      val g = """{
        "scopeName": "source.z",
        "patterns": [ { "match": "(a)b", "captures": { "0": { "name": "meta.z" }, "1": { "name": "keyword.z" } } } ]
      }"""
      scopesOfToken(classHl(g), "ab", "a") shouldBe "source.z meta.z keyword.z"
    }
  }

  "applyEndPatternLast" - {
    def endLast(flag: String): String =
      s"""{ "scopeName": "source.l", "patterns": [ { "begin": "\\\\[", "end": "\\\\]", "name": "meta.l", $flag "patterns": [ { "match": "\\\\]\\\\]", "name": "keyword.l" } ] } ] }"""

    "applyEndPatternLast: the end wins a tie by default" in {
      classHl(endLast("")).highlight("[a]] b") should not include "hl-keyword"
    }

    "applyEndPatternLast: under it an inner rule wins the tie" in {
      classHl(endLast("\"applyEndPatternLast\": 1,")).highlight("[a]] b") should include(
        """<span class="hl-keyword">]]</span>""")
    }
  }

  "the loop guards" - {
    "guards: a pair that opens and closes at one place matching nothing stays open, and the line ends" in {
      val g = """{
        "scopeName": "source.p",
        "patterns": [ { "begin": "(?=x)", "end": "(?=x)", "name": "string.p" } ]
      }"""
      classHl(g).highlight("axy\nz") shouldBe
        """a<span class="hl-string">xy</span>""" + "\n" + """<span class="hl-string">z</span>"""
    }

    "guards: a pair opening again where it already is, matching nothing, does not" in {
      val g = """{
        "scopeName": "source.o",
        "patterns": [ { "begin": "(?=y)", "end": "z", "name": "string.o", "patterns": [ { "include": "$self" } ] } ]
      }"""
      classHl(g).highlight("xy") shouldBe """x<span class="hl-string">y</span>"""
    }

    "$base: in a grammar on its own it is the grammar itself" in {
      val g = """{
        "scopeName": "source.b",
        "patterns": [
          { "match": "\\bkw\\b", "name": "keyword.b" },
          { "begin": "\\(", "end": "\\)", "patterns": [ { "include": "$base" } ] }
        ]
      }"""
      classHl(g).highlight("(kw)") should include("""<span class="hl-keyword">kw</span>""")
    }
  }

  // The category group's tests also have a Scala original ("the category is read from the whole scope stack");
  // these are the sysl suite's renderings of the same stack-reading rule, over a grammar of its own.
  "the sysl suite's stacked-scope renderings" - {
    val stacked = """{
      "scopeName": "source.stacked",
      "patterns": [
        { "begin": "\"", "end": "\"", "name": "string.quoted.double.stacked",
          "beginCaptures": { "0": { "name": "punctuation.definition.string.begin.stacked" } },
          "endCaptures": { "0": { "name": "punctuation.definition.string.end.stacked" } },
          "patterns": [
            { "match": ",", "name": "punctuation.separator.stacked" },
            { "match": "_+", "name": "meta.foo.stacked" }
          ] },
        { "match": "(//).*$", "name": "comment.line.stacked",
          "captures": { "1": { "name": "punctuation.definition.comment.stacked" } } },
        { "match": "<", "name": "punctuation.definition.tag.stacked" }
      ]
    }"""

    "a string's quotes take the string category" in {
      classHl(stacked).highlight("\"hi\"") shouldBe
        """<span class="hl-string">&quot;</span><span class="hl-string">hi</span><span class="hl-string">&quot;</span>"""
    }

    "a line comment's marker takes the comment category" in {
      classHl(stacked).highlight("// x") shouldBe
        """<span class="hl-comment">//</span><span class="hl-comment"> x</span>"""
    }

    "a punctuation.definition scope with no categorised parent stays punctuation" in {
      classHl(stacked).highlight("<") shouldBe """<span class="hl-punctuation">&lt;</span>"""
    }

    "a punctuation scope that is not a definition stays punctuation inside a string" in {
      classHl(stacked).highlight("\"a,b\"") shouldBe
        """<span class="hl-string">&quot;</span><span class="hl-string">a</span><span class="hl-punctuation">,</span><span class="hl-string">b</span><span class="hl-string">&quot;</span>"""
    }

    "an uncategorised innermost scope takes the category of the scope enclosing it" in {
      classHl(stacked).highlight("\"a__b\"") shouldBe
        """<span class="hl-string">&quot;</span><span class="hl-string">a</span><span class="hl-string">__</span><span class="hl-string">b</span><span class="hl-string">&quot;</span>"""
    }

    "category_of reads the whole stack and is the category the renderer uses" in {
      val h = classHl(stacked)
      h.categoryOf(h.tokens("\"hi\"").head.head) shouldBe "string"
    }
  }
}
