package com.nulabinc.backlog.c2b.core.domain.parser

import java.time.{ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.core.domain.model.{CybozuComment, CybozuUser}
import com.nulabinc.backlog.c2b.core.utils.ZonedDateTimeUtil

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
              ZonedDateTimeUtil.toZonedDateTime(createdAt) match {
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

}
