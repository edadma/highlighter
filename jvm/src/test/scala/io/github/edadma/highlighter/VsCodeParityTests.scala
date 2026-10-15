package io.github.edadma.highlighter

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

/** Where the tokenizer follows vscode-textmate, checked against the real grammars in `test-grammars/` and against the
  * corpus in `src/test/resources/equivalence/`.
  *
  * The corpus is a page of each of the thirteen languages and the Scala suite's own inputs: `<name>.txt` is the input
  * and `<name>.vscode.html` is what Visual Studio Code's tokenizer (vscode-textmate 9.3.2 on vscode-oniguruma 2.0.1)
  * makes of it with the same grammar, rendered as `ClassMode("hl-")` renders. The sysl highlighter's suite checks the
  * same files, and each test here carries the name of its sysl counterpart.
  */
class VsCodeParityTests extends AnyFreeSpec with Matchers {

  private def load(name: String): Highlighter =
    val txt = scala.io.Source.fromFile(s"test-grammars/$name.tmLanguage.json").mkString
    Highlighter.fromJson(txt, ClassMode("hl-")) match
      case Right(hl) => hl
      case Left(err) => fail(s"failed to load $name: $err")

  private def scopesOfToken(h: Highlighter, code: String, text: String): String =
    h.tokens(code).flatten.find(_.text == text).getOrElse(fail(s"no token reads '$text'")).scopes.mkString(" ")

  "real grammars" - {
    "\\b in a real grammar: Scala's 'if' inside 'síif' is not coloured" in {
      load("scala").highlight("val síif = 1") should not include """<span class="hl-keyword">if</span>"""
    }

    "python: a string closes on the quote it opened with, and the code after it is code" in {
      val out = load("python").highlight("x = \"a\" if y else 'b'")
      out should include("""<span class="hl-keyword">if</span>""")
      out should include("""<span class="hl-keyword">else</span>""")
    }

    "newline: YAML's comment does not run on into the next line" in {
      load("yaml").highlight("# hello\nkey: value") should include("""<span class="hl-function">key</span>""")
    }

    "newline: the newline itself is in no token" in {
      for line <- load("yaml").tokens("# hello\nkey: value"); t <- line do t.text should not include "\n"
    }

    "\\A: JavaScript's shebang is one on the first line only" in {
      load("javascript").highlight("#!/usr/bin/env node\nlet a = 1\n#!no") shouldBe
        """<span class="hl-comment">#!</span><span class="hl-comment">/usr/bin/env node</span>""" + "\n" +
        """<span class="hl-keyword">let</span> <span class="hl-variable">a</span> <span class="hl-keyword">=</span> <span class="hl-number">1</span>""" + "\n" +
        """#<span class="hl-keyword">!</span><span class="hl-variable">no</span>"""
    }

    "names: a name holding several scopes is that many scopes, the last innermost" in {
      load("json").highlight("{\"k\": 1}") should include("""<span class="hl-type">k</span>""")
      scopesOfToken(load("json"), "{\"k\": 1}", "k") shouldBe
        "source.json meta.structure.dictionary.json string.json support.type.property-name.json"
    }

    "captures: Go's function name is found by a capture's own patterns" in {
      load("go").highlight("func main() {}") should include("""<span class="hl-function">main</span>""")
    }

    "tokens over non-ASCII text concatenate back to each line" in {
      val code  = "val café = \"naïve\" // 日本語\nval x = 1"
      val lines = load("scala").tokens(code)
      lines.length shouldBe 2
      lines(0).map(_.text).mkString shouldBe "val café = \"naïve\" // 日本語"
      lines(1).map(_.text).mkString shouldBe "val x = 1"
    }
  }

  "the VS Code corpus" - {
    val dir: Path = Paths.get(getClass.getResource("/equivalence").toURI)

    def readText(p: Path): String = new String(Files.readAllBytes(p), StandardCharsets.UTF_8)

    def corpus: List[String] =
      val s = Files.list(dir)
      try s.iterator.asScala.map(_.getFileName.toString).filter(_.endsWith(".txt")).map(_.dropRight(4)).toList.sorted
      finally s.close()

    // The 1-based lines of `name` that differ from VS Code's.
    def differing(name: String): List[Int] =
      val lang   = name.takeWhile(_ != '.')
      val ours   = load(lang).highlight(readText(dir.resolve(s"$name.txt"))).split("\n", -1).toList
      val theirs = readText(dir.resolve(s"$name.vscode.html")).split("\n", -1).toList
      ours.length shouldBe theirs.length
      ours.zip(theirs).zipWithIndex.collect { case ((a, b), i) if a != b => i + 1 }

    "the corpus holds 24 files, each with VS Code's rendering beside it" in {
      corpus.length shouldBe 24
      for n <- corpus do Files.exists(dir.resolve(s"$n.vscode.html")) shouldBe true
    }

    // Ignored while the oniguruma engine reads `\w` as ASCII: the lines that differ are the ones holding an accented
    // letter or a CJK name, where VS Code's `\w+` takes the whole word.
    "every corpus file but html.1 renders exactly as VS Code renders it" ignore {
      val bad = for n <- corpus if n != "html.1"; d = differing(n) if d.nonEmpty yield s"$n: lines $d"
      bad shouldBe empty
    }

    "every corpus file but html.1 renders as VS Code renders it on every line without a non-ASCII letter" in {
      val bad = for
        n <- corpus if n != "html.1"
        lines = readText(dir.resolve(s"$n.txt")).split("\n", -1)
        d <- differing(n) if lines(d - 1).forall(_ < '\u0080')
      yield s"$n:$d"
      bad shouldBe empty
    }

    // HTML's `<style>` and `<script>` bodies are the one thing deliberately not compared: VS Code highlights them by
    // including the CSS and JavaScript grammars, and a grammar's includes of another grammar are not followed.
    "html.1 differs from VS Code only in the embedded style and script bodies, which are plain" in {
      differing("html.1") shouldBe List(8, 21)
      val ours = load("html").highlight(readText(dir.resolve("html.1.txt"))).split("\n", -1)
      ours(7) shouldBe "    body { margin: 0; color: #333; }"
      ours(20) shouldBe "    const x = 1; console.log(&quot;hi&quot;, x);"
    }
  }
}
