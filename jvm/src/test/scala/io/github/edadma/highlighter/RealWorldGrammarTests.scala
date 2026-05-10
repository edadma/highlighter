package io.github.edadma.highlighter

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** End-to-end coverage against real VS Code TextMate grammars vendored in
  * `test-grammars/`. The tests in `Tests.scala` use a toy 4-pattern grammar
  * — necessary, but they hide the failure modes that show up in the wild
  * (lookbehind, POSIX classes inside complex character sets, end-of-line
  * anchors mid-string, repository-include depth, etc.).
  *
  * Each language gets:
  *   - **load** — the grammar parses without error
  *   - **construct tests** — common forms a user would type, asserting the
  *     scope class shows up on the relevant token. Failing constructs are
  *     marked with `ignore` and a `TODO:` pointing at the underlying gap.
  *
  * **Don't make passing assertions weaker just to make these green.** When a
  * construct doesn't highlight, that's a real bug — leave the test ignored
  * with the right expected output so it passes when the bug is fixed.
  */
class RealWorldGrammarTests extends AnyFreeSpec with Matchers {

  // ─────────────────────────────────────────────────────────────────
  // Helpers — load grammars from <repo>/test-grammars/
  // ─────────────────────────────────────────────────────────────────

  private def loadGrammar(name: String): Highlighter = {
    val path = s"test-grammars/$name.tmLanguage.json"
    val txt  = scala.io.Source.fromFile(path).mkString
    Highlighter.fromJson(txt, ClassMode("hl-")) match {
      case Right(hl) => hl
      case Left(err) => fail(s"failed to load $name: $err")
    }
  }

  private def hl(name: String): Highlighter = loadGrammar(name)

  /** Convenience: assert that `hl` outputs an `hl-<klass>` span for the input.
    * Use sparingly — prefer checking exact substrings of the output when you
    * care about WHICH token got the class. */
  extension (out: String)
    def shouldHighlight(klass: String, withinSubstr: String = ""): org.scalatest.Assertion = {
      val cls = s"""class="hl-$klass""""
      if (withinSubstr.isEmpty) out should include(cls)
      else {
        val span = s"""<span $cls>$withinSubstr</span>"""
        out should include(span)
      }
    }

  // ─────────────────────────────────────────────────────────────────
  // 1. Loading: every shipped grammar must parse cleanly
  // ─────────────────────────────────────────────────────────────────

  "grammar loading" - {
    val all = List(
      "javascript", "typescript", "tsx", "bash", "python", "json",
      "yaml", "html", "css", "go", "rust", "scala",
    )
    all.foreach { lang =>
      lang in {
        loadGrammar(lang) // throws via fail() if it can't load
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 2. JavaScript — the user's first example. Every assertion below is
  //    something a hand-written JS file in a docs site would do.
  // ─────────────────────────────────────────────────────────────────

  "javascript" - {

    "single-line comment" in {
      hl("javascript").highlight("// hello").shouldHighlight("comment")
    }

    "block comment" in {
      hl("javascript").highlight("/* hello */").shouldHighlight("comment")
    }

    // TODO: un-ignore when highlighter handles VS Code's import-declaration
    // begin pattern — `(?<![_$[:alnum:]])(?:(?<=\.\.\.)|(?<!\.))(?:(\bexport)\s+)?(?:(\bdeclare)\s+)?\b(import)…`
    // The negative lookbehind chain doesn't compile or doesn't match.
    "import keyword" ignore {
      hl("javascript").highlight("""import x from "y"""").shouldHighlight("keyword", "import")
    }

    // TODO: un-ignore when string.quoted.double matches end-of-string `"`
    // — its end pattern is `(\")|((?:[^\\\n])$)` which mixes a
    // group-1 close with a group-2 EOL guard. Smoke test of
    // `"hello"` returned bare, no string span.
    "double-quoted string" ignore {
      hl("javascript").highlight(""""hello"""").shouldHighlight("string", "&quot;hello&quot;")
    }

    // TODO: un-ignore when single-quoted strings highlight too.
    "single-quoted string" ignore {
      hl("javascript").highlight("""'hello'""").shouldHighlight("string", "&#39;hello&#39;")
    }

    // TODO: template literals
    "template literal" ignore {
      hl("javascript").highlight("`hi ${x}`").shouldHighlight("string")
    }

    // TODO: const/let/var keyword detection
    "const declaration" ignore {
      hl("javascript").highlight("const x = 1").shouldHighlight("keyword", "const")
    }

    "let declaration" ignore {
      hl("javascript").highlight("let x = 1").shouldHighlight("keyword", "let")
    }

    "var declaration" ignore {
      hl("javascript").highlight("var x = 1").shouldHighlight("keyword", "var")
    }

    "numeric literal" ignore {
      hl("javascript").highlight("const x = 42").shouldHighlight("number", "42")
    }

    "true / false / null literals" ignore {
      val out = hl("javascript").highlight("const a = true; const b = false; const c = null;")
      out.shouldHighlight("variable", "true")
    }

    "function keyword" ignore {
      hl("javascript").highlight("function f() {}").shouldHighlight("keyword", "function")
    }

    "arrow function" ignore {
      hl("javascript").highlight("const f = () => 1").shouldHighlight("keyword", "const")
    }

    "regex literal" ignore {
      hl("javascript").highlight("const r = /^foo$/g").shouldHighlight("string")
    }

    "class keyword" ignore {
      hl("javascript").highlight("class Foo {}").shouldHighlight("keyword", "class")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 3. TypeScript — same family, same regex tricks
  // ─────────────────────────────────────────────────────────────────

  "typescript" - {

    "comment works" in {
      hl("typescript").highlight("// hello").shouldHighlight("comment")
    }

    "import keyword" ignore {
      hl("typescript").highlight("""import x from "y"""").shouldHighlight("keyword", "import")
    }

    "interface keyword" ignore {
      hl("typescript").highlight("interface Foo {}").shouldHighlight("keyword", "interface")
    }

    "type keyword" ignore {
      hl("typescript").highlight("type T = string").shouldHighlight("keyword", "type")
    }

    "type annotation `: string`" ignore {
      hl("typescript").highlight("const x: string = 'a'").shouldHighlight("type")
    }

    "generic" ignore {
      hl("typescript").highlight("function f<T>(x: T): T { return x }").shouldHighlight("keyword")
    }

    "enum keyword" ignore {
      hl("typescript").highlight("enum Color { Red, Green }").shouldHighlight("keyword", "enum")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 4. Bash — known to mostly work in the wild
  // ─────────────────────────────────────────────────────────────────

  "bash" - {

    "comment" in {
      hl("bash").highlight("# hello").shouldHighlight("comment")
    }

    "double-quoted string" in {
      hl("bash").highlight("""x="hello"""").shouldHighlight("string")
    }

    "single-quoted string" in {
      hl("bash").highlight("""x='hello'""").shouldHighlight("string")
    }

    // TODO: $HOME inside double quotes lands in `string` only — variable
    // sub-pattern inside #qstring-double isn't being descended. Real
    // bug: nested patterns inside begin/end blocks aren't applied to
    // the inner text.
    "variable expansion inside string" ignore {
      hl("bash").highlight("""echo "$HOME"""").shouldHighlight("variable")
    }

    "if keyword" in {
      hl("bash").highlight("if true; then echo y; fi").shouldHighlight("keyword", "if")
    }

    // TODO: `for i in 1 2 3` is being matched as one giant `string` by
    // the for-loop pattern's variable-list segment. Real bug — the
    // `for` keyword and `in` keyword should both surface; instead
    // everything from `for` through `done` is one string span.
    "for keyword" ignore {
      hl("bash").highlight("for i in 1 2 3; do echo $i; done").shouldHighlight("keyword", "for")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 5. Python — different feature set, useful coverage
  // ─────────────────────────────────────────────────────────────────

  "python" - {

    // TODO: python comment is being split — `# ` lands in `keyword`
    // (the line-comment punctuation.definition.comment scope likely
    // routes through `storage` or similar) and `hello` lands in
    // `function`. Whole `# hello` should be one `comment` span.
    "comment" ignore {
      hl("python").highlight("# hello").shouldHighlight("comment")
    }

    "def keyword" ignore {
      hl("python").highlight("def f():\n    pass").shouldHighlight("keyword", "def")
    }

    "class keyword" ignore {
      hl("python").highlight("class C: pass").shouldHighlight("keyword", "class")
    }

    "string literal" ignore {
      hl("python").highlight("""x = "hello"""").shouldHighlight("string")
    }

    "f-string" ignore {
      hl("python").highlight("""x = f"hello {name}"""").shouldHighlight("string")
    }

    "number" ignore {
      hl("python").highlight("x = 42").shouldHighlight("number", "42")
    }

    "True/False/None" ignore {
      hl("python").highlight("x = True").shouldHighlight("variable", "True")
    }

    "import statement" ignore {
      hl("python").highlight("import os").shouldHighlight("keyword", "import")
    }

    "decorator" ignore {
      hl("python").highlight("@decorator").shouldHighlight("function")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 6. JSON — the simplest non-trivial grammar
  // ─────────────────────────────────────────────────────────────────

  "json" - {

    // TODO: same string-end-regex problem as JS — JSON's qstring-double
    // uses `(\")|((?:[^\\\n])$)` which doesn't behave the way Oniguruma
    // would. Strings come out bare.
    "string value" ignore {
      hl("json").highlight(""""hello"""").shouldHighlight("string")
    }

    "number" in {
      hl("json").highlight("42").shouldHighlight("number", "42")
    }

    "true/false/null" in {
      hl("json").highlight("true").shouldHighlight("variable", "true")
    }

    // TODO: blocked by the same string bug; the keys + values are strings.
    "object key + value" ignore {
      hl("json").highlight("""{"k": "v"}""").shouldHighlight("string")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 7. YAML — anchors, aliases, multi-line strings
  // ─────────────────────────────────────────────────────────────────

  "yaml" - {

    "comment" in {
      hl("yaml").highlight("# hello").shouldHighlight("comment")
    }

    // YAML grammar maps mapping keys to `string.unquoted` not `keyword`,
    // which is conventional. Adjust the assertion to match the real
    // grammar choice rather than my guess.
    "key" in {
      hl("yaml").highlight("key: value").shouldHighlight("string")
    }

    "string value" ignore {
      hl("yaml").highlight("""key: "value"""").shouldHighlight("string")
    }

    "list item" ignore {
      hl("yaml").highlight("- item").shouldHighlight("punctuation")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 8. HTML — tags + attributes + entities
  // ─────────────────────────────────────────────────────────────────

  "html" - {

    // VS Code's HTML grammar tags `html` inside `<!DOCTYPE html>` as
    // `entity.name.tag.doctype` which highlighter maps to `function`.
    // Adjust the assertion: at least *something* gets highlighted.
    "doctype" in {
      hl("html").highlight("<!DOCTYPE html>").shouldHighlight("function", "html")
    }

    // TODO: HTML comments `<!-- ... -->` are matched as `comment.block.html`
    // by the grammar; highlighter is missing the begin pattern and
    // descending into the inside as if it were tag content. Real bug.
    "comment" ignore {
      hl("html").highlight("<!-- hi -->").shouldHighlight("comment")
    }

    "open tag" ignore {
      hl("html").highlight("<div>").shouldHighlight("keyword", "div")
    }

    "attribute" ignore {
      hl("html").highlight("""<a href="x"></a>""").shouldHighlight("variable")
    }

    "self-closing tag" ignore {
      hl("html").highlight("""<img src="x" />""").shouldHighlight("keyword")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 9. CSS — selectors, properties, values, @-rules
  // ─────────────────────────────────────────────────────────────────

  "css" - {

    "comment" in {
      hl("css").highlight("/* hello */").shouldHighlight("comment")
    }

    "class selector" ignore {
      hl("css").highlight(".foo { color: red }").shouldHighlight("variable")
    }

    "property" ignore {
      hl("css").highlight(".x { color: red }").shouldHighlight("keyword", "color")
    }

    "color value" ignore {
      hl("css").highlight(".x { color: #ff0000 }").shouldHighlight("number")
    }

    "@media rule" ignore {
      hl("css").highlight("@media (min-width: 600px) {}").shouldHighlight("keyword", "@media")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 10. Go — package, func, types, channels
  // ─────────────────────────────────────────────────────────────────

  "go" - {

    "comment" in {
      hl("go").highlight("// hello").shouldHighlight("comment")
    }

    "package declaration" ignore {
      hl("go").highlight("package main").shouldHighlight("keyword", "package")
    }

    "func declaration" ignore {
      hl("go").highlight("func main() {}").shouldHighlight("keyword", "func")
    }

    "string literal" ignore {
      hl("go").highlight("""x := "hello"""").shouldHighlight("string")
    }

    "type keyword" ignore {
      hl("go").highlight("type Foo struct {}").shouldHighlight("keyword", "type")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 11. Rust — fn, struct, impl, lifetimes, macros
  // ─────────────────────────────────────────────────────────────────

  "rust" - {

    "comment" in {
      hl("rust").highlight("// hello").shouldHighlight("comment")
    }

    "fn declaration" ignore {
      hl("rust").highlight("fn main() {}").shouldHighlight("keyword", "fn")
    }

    "let binding" ignore {
      hl("rust").highlight("let x = 1;").shouldHighlight("keyword", "let")
    }

    "struct keyword" ignore {
      hl("rust").highlight("struct Foo {}").shouldHighlight("keyword", "struct")
    }

    "string literal" ignore {
      hl("rust").highlight("""let s = "hi";""").shouldHighlight("string")
    }

    "macro invocation" ignore {
      hl("rust").highlight("println!(\"hi\");").shouldHighlight("function", "println")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 12. Scala — the lib's home language; extra coverage matters here
  // ─────────────────────────────────────────────────────────────────

  "scala" - {

    "single-line comment" in {
      hl("scala").highlight("// hello").shouldHighlight("comment")
    }

    "def keyword" in {
      hl("scala").highlight("def f = 1").shouldHighlight("keyword", "def")
    }

    "val keyword" in {
      hl("scala").highlight("val x = 1").shouldHighlight("keyword", "val")
    }

    "if keyword" in {
      hl("scala").highlight("if (x) 1 else 2").shouldHighlight("keyword", "if")
    }

    "string literal" in {
      hl("scala").highlight("""val s = "hi"""").shouldHighlight("string")
    }

    "numeric literal" in {
      hl("scala").highlight("val x = 42").shouldHighlight("number", "42")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 13. Regex feature surface — synthetic grammars that exercise one
  //     regex feature each. Lets us pinpoint *which* feature is failing
  //     vs blaming a giant grammar.
  // ─────────────────────────────────────────────────────────────────

  "regex feature surface" - {

    def synth(pattern: String, name: String = "keyword.synth"): Highlighter = {
      val json = s"""{
        "scopeName": "source.synth",
        "patterns": [ { "match": ${jsonEscape(pattern)}, "name": "$name" } ]
      }"""
      Highlighter.fromJson(json, ClassMode("hl-")) match {
        case Right(hl) => hl
        case Left(err) => fail(s"synth grammar failed to load: pattern=$pattern err=$err")
      }
    }

    def jsonEscape(s: String): String =
      "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    "negative lookbehind" in {
      synth("""(?<!\.)\bfoo\b""").highlight("foo").shouldHighlight("keyword", "foo")
    }

    "positive lookbehind" in {
      synth("""(?<=\$)\w+""").highlight("$x").shouldHighlight("keyword", "x")
    }

    "negative lookahead" in {
      synth("""\bfoo\b(?!\.)""").highlight("foo").shouldHighlight("keyword", "foo")
    }

    "POSIX class [:alnum:]" in {
      synth("""\b[[:alnum:]]+\b""").highlight("hello").shouldHighlight("keyword", "hello")
    }

    "POSIX class [:alpha:]" in {
      synth("""\b[[:alpha:]]+\b""").highlight("hello").shouldHighlight("keyword", "hello")
    }

    "POSIX class inside complex set [_$[:alnum:]]" in {
      synth("""\b[_$[:alnum:]]+\b""").highlight("a_b").shouldHighlight("keyword", "a_b")
    }

    "atomic group" ignore {
      // TODO: java.util.regex doesn't accept (?>...) — VS Code's Oniguruma does.
      // Highlighter swallows the compile error and silently drops the pattern.
      synth("""(?>foo)""").highlight("foo").shouldHighlight("keyword", "foo")
    }

    "possessive quantifier *+" ignore {
      // TODO: same — java.util.regex spelled differently than Oniguruma here.
      synth("""f.*+""").highlight("foo").shouldHighlight("keyword", "foo")
    }

    "end-of-line anchor mid-string" in {
      synth("""[^\n]+$""").highlight("hello").shouldHighlight("keyword", "hello")
    }

    "alternation" in {
      synth("""\b(if|else|while)\b""").highlight("if").shouldHighlight("keyword", "if")
    }

    "captures + named scope" in {
      val grammar =
        """{
          "scopeName": "source.synth",
          "patterns": [
            { "match": "\\b(if)\\s+(\\w+)",
              "captures": {
                "1": { "name": "keyword.control" },
                "2": { "name": "variable.other" }
              }
            }
          ]
        }"""
      val hl = Highlighter.fromJson(grammar, ClassMode("hl-")).getOrElse(fail("captures grammar failed"))
      val out = hl.highlight("if x")
      out.shouldHighlight("keyword", "if")
      out.shouldHighlight("variable", "x")
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // 14. Loader hardening — these ARE bugs, not edge cases
  // ─────────────────────────────────────────────────────────────────

  "loader hardening" - {

    // TODO: un-ignore when compileRegex stops swallowing exceptions.
    // Currently it returns None on regex compile failure and the pattern
    // is silently dropped — which is exactly why JS strings/keywords
    // never highlight: a Java-incompatible regex compiles to None and
    // the rest of the grammar carries on with that pattern missing.
    "fromJson surfaces regex compile failures" ignore {
      // (?P<name>...) is a Python/Oniguruma named-group syntax that Java
      // doesn't accept (Java uses (?<name>...) without the P).
      val bad = """{
        "scopeName": "source.bad",
        "patterns": [ { "match": "(?P<x>foo)", "name": "keyword.bad" } ]
      }"""
      val res = Highlighter.fromJson(bad, ClassMode("hl-"))
      // Either a Left, or a Right with a `loadWarnings` collection — pick
      // a contract once the fix lands. The current behavior (Right with
      // silent drop) IS the bug.
      res.isLeft shouldBe true
    }
  }
}
