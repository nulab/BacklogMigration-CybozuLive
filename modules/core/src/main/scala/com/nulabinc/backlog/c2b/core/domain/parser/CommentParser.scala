package com.nulabinc.backlog.c2b.core.domain.parser

import com.nulabinc.backlog.c2b.core.domain.model.CybozuComment

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
          val pattern = """(\d+)?: (.+? .+?) (.+)""".r

          header match {
            case pattern(id, userString, createdAtString) =>
              (for {
                user      <- UserParser.toUser(userString)
                createdAt <- ZonedDateTimeParser.toZonedDateTime(createdAtString)
              } yield {
                  CybozuComment(
                    id = id.toLong,
                    creator = user,
                    createdAt = createdAt,
                    content = body.mkString("\n")
                  )

              }) match {
                case Right(a) => Right(a)
                case Left(error) =>
                  Left(CannotParseComment("Header parsing error.", error.toString))
              }
            case _ => Left(CannotParseComment("Invalid header", comment))
          }
        } catch {
          case ex: Throwable => Left(CannotParseComment(ex.getMessage, comment))
        }
      }
  }

}
