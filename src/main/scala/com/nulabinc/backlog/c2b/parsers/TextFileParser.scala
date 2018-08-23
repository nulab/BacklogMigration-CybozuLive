package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.{CybozuTextPost, CybozuTextTopic}

object TextFileParser {

  private val postPattern = """(?ms)\r\n(\d+)?: (.+? .+?) (.+?)\r\n\r\n(.*?)\r\n\r\n--------------------------------------------------------------------------------""".r
  private val MIN_TOPIC_LINES = 4
  private val TITLE_LINE_INDEX = 1
  private val DESCRIPRION_START_INDEX = 3

  def topic(topicText: String): Either[ParseError[CybozuTextTopic], CybozuTextTopic] = {
    val lines = topicText.split("\n")

    if (lines.length < MIN_TOPIC_LINES)
      Left(CannotParseFromString(classOf[CybozuTextTopic], "Invalid topic rows: min", topicText))
    else {
      val result = for {
        title <- title(lines(TITLE_LINE_INDEX))
        description <- description(lines.slice(DESCRIPRION_START_INDEX, lines.length))
      } yield CybozuTextTopic(title, description)
      result match {
        case Right(value) => Right(value)
        case Left(error) => Left(CannotParseFromString(classOf[CybozuTextTopic], error.getMessage, lines.mkString("\n")))
      }
    }
  }

  def post(postStr: String): Either[ParseError[CybozuTextPost], CybozuTextPost] = ???

  private[parsers] def title(line: String): Either[Throwable, String] = {
    val regex = """.+?: (.+?)""".r

    line match {
      case regex(title) => Right(title)
      case _ => Left(new RuntimeException("Cannot find topic title"))
    }
  }

  private[parsers] def description(lines: Seq[String]): Either[Throwable, String] =
    Right(lines.mkString("\n"))

}
