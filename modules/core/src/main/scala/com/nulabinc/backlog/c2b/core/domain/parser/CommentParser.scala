package com.nulabinc.backlog.c2b.core.domain.parser

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import com.nulabinc.backlog.c2b.core.domain.model.{CybozuComment, CybozuUser}

object CommentParser {

  val separator: String = "--------------------------------------------------"

  def parse(comments: String): Seq[Either[ParseError[CybozuComment] , CybozuComment]] = {
    comments
      .split(separator)
      .filterNot(_.isEmpty)
      .filterNot(_ == "\n")
      .map { comment =>
        try {
          val lines = comment.split("\n").tail
          val header = lines.head
          val body = lines.tail.tail
          val pattern = """(\d+)?: (.+?) (.+?) (.+)""".r

          header match {
            case pattern(id, firstName, lastName, createdAt) =>
              toZonedDateTime(createdAt) match {
                case Some(parsedZonedDateTime) =>
                  Right(
                    CybozuComment(
                      id        = id.toLong,
                      creator   = CybozuUser(firstName = firstName, lastName = lastName),
                      createdAt = parsedZonedDateTime,
                      content   = body.mkString("\n")
                    )
                  )
                case None => Left(CannotParseComment("Invalid DateTime", createdAt))
              }
            case _ => Left(CannotParseComment("Invalid header", comment))
          }
        } catch {
          case ex: Throwable => Left(CannotParseComment(ex.getMessage, comment))
        }
      }
  }

  def toZonedDateTime(value: String): Option[ZonedDateTime] = {
    val pattern = """(\d+?)/(\d+?)/(\d+?) .+? (\d+?):(\d+?)""".r
    value match {
      case pattern(year, month, day, hour, minutes) =>
        Some(
          ZonedDateTime.of(
            year.toInt,
            month.toInt,
            day.toInt,
            hour.toInt,
            minutes.toInt,
            0,
            0,
            ZoneId.systemDefault()
          )
        )
      case _ => None
    }
  }
}
