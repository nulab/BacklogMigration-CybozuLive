package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.{CybozuTextPost, CybozuTextTopic, CybozuTextUser}

object TextFileParser {

  private val titlePattern = """.+?: (.+?)""".r
  private val MIN_TOPIC_LINES = 2
  private val TITLE_LINE_INDEX = 1
  private val DESCRIPRION_START_INDEX = 3

  // post
  private val MIN_POST_LINES = 5
  private val userPattern = """\d+?: (.+?)""".r
  private val USER_LINE_INDEX = 1
  private val POSTED_AT_LINE_INDEX = 2
  private val CONTENT_START_INDEX = 4

  def topic(topicText: String): Either[ParseError[CybozuTextTopic], CybozuTextTopic] = {
    val lines = topicText.split("\n")
    val lineLength = lines.length

    if (lineLength < MIN_TOPIC_LINES)
      Left(CannotParseFromString(classOf[CybozuTextTopic], s"Invalid topic row length: $lineLength. Required: $MIN_TOPIC_LINES", topicText))
    else {
      val result = for {
        title <- title(lines(TITLE_LINE_INDEX))
        description = if (lineLength < DESCRIPRION_START_INDEX) "" else arrayToString(lines.slice(DESCRIPRION_START_INDEX, lineLength))
      } yield CybozuTextTopic(title, description)
      result match {
        case Right(value) => Right(value)
        case Left(error) => Left(CannotParseFromString(classOf[CybozuTextTopic], error.getMessage, lines.mkString("\n")))
      }
    }
  }

  def post(postStr: String): Either[ParseError[CybozuTextPost], CybozuTextPost] = {
    val lines = postStr.split("\n")

    if (lines.length < MIN_POST_LINES)
      Left(CannotParsePost("Invalid post rows: min", postStr))
    else {
      val dateString = lines(POSTED_AT_LINE_INDEX)
      val postedAtResult = ZonedDateTimeParser.toZonedDateTime(dateString) match {
        case Right(value) => Right(value)
        case Left(error) => Left(CannotParseFromString(classOf[CybozuTextPost], error.toString, dateString))
      }
      for {
        postedAt <- postedAtResult
        user <- user(lines(USER_LINE_INDEX))
      } yield CybozuTextPost(
        content = arrayToString(lines.slice(CONTENT_START_INDEX, lines.length)),
        postUser = CybozuTextUser(user),
        postedAt = postedAt
      )
    }
  }

  private[parsers] def title(line: String): Either[Throwable, String] =
    line match {
      case titlePattern(title) => Right(title)
      case _ => Left(new RuntimeException("Cannot find topic title"))
    }

  private[parsers] def arrayToString(lines: Seq[String]): String =
    lines.mkString("\n")

  private[parsers] def user(line: String): Either[ParseError[CybozuTextPost], String] =
    line match {
      case userPattern(user) => Right(user)
      case _ => Left(CannotParseFromString(classOf[CybozuTextPost], "Cannot find user string", line))
    }

}
