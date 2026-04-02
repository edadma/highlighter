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
}
