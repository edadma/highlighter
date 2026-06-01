package io.github.edadma.highlighter

import zio.json.*
import zio.json.ast.Json

case class CaptureEntry(name: Option[String] = None, patterns: Option[List[Pattern]] = None)

object CaptureEntry:
  given JsonDecoder[CaptureEntry] = DeriveJsonDecoder.gen[CaptureEntry]

  /** Lenient decoder for a TextMate `captures` / `beginCaptures` /
    * `endCaptures` map. Capture keys are normally capture-group numbers
    * (`"0"`, `"1"`, …) whose values are capture descriptors. Some
    * real-world grammars — e.g. Atom's XML grammar's `comments` block —
    * misplace stray scalar keys like `"end"` / `"name"` *inside* a
    * `captures` object. VS Code's tokenizer simply ignores anything that
    * isn't a numbered capture descriptor; matching that, we skip any
    * entry whose value isn't a capture object rather than failing the
    * entire grammar load over one malformed sibling. */
  given JsonDecoder[Map[String, CaptureEntry]] =
    JsonDecoder[Map[String, Json]].map { raw =>
      raw.flatMap { case (k, v) => v.as[CaptureEntry].toOption.map(k -> _) }
    }

case class Grammar(
    scopeName: String,
    name: Option[String] = None,
    fileTypes: Option[List[String]] = None,
    patterns: List[Pattern] = Nil,
    repository: Option[Map[String, RepositoryEntry]] = None,
)

object Grammar:
  given JsonDecoder[Grammar] = DeriveJsonDecoder.gen[Grammar]

case class RepositoryEntry(
    name: Option[String] = None,
    `match`: Option[String] = None,
    begin: Option[String] = None,
    end: Option[String] = None,
    captures: Option[Map[String, CaptureEntry]] = None,
    beginCaptures: Option[Map[String, CaptureEntry]] = None,
    endCaptures: Option[Map[String, CaptureEntry]] = None,
    patterns: Option[List[Pattern]] = None,
    include: Option[String] = None,
    contentName: Option[String] = None,
)

object RepositoryEntry:
  given JsonDecoder[RepositoryEntry] = DeriveJsonDecoder.gen[RepositoryEntry]

  def toPattern(entry: RepositoryEntry): Pattern =
    Pattern(
      name = entry.name,
      `match` = entry.`match`,
      begin = entry.begin,
      end = entry.end,
      captures = entry.captures,
      beginCaptures = entry.beginCaptures,
      endCaptures = entry.endCaptures,
      patterns = entry.patterns,
      include = entry.include,
      contentName = entry.contentName,
    )

case class Pattern(
    name: Option[String] = None,
    `match`: Option[String] = None,
    begin: Option[String] = None,
    end: Option[String] = None,
    captures: Option[Map[String, CaptureEntry]] = None,
    beginCaptures: Option[Map[String, CaptureEntry]] = None,
    endCaptures: Option[Map[String, CaptureEntry]] = None,
    patterns: Option[List[Pattern]] = None,
    include: Option[String] = None,
    contentName: Option[String] = None,
)

object Pattern:
  given JsonDecoder[Pattern] = DeriveJsonDecoder.gen[Pattern]
