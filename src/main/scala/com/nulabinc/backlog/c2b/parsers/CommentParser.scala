package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.{CybozuCSVComment, CybozuCSVUser}

object CommentParser {

  val separator: String = "^--------------------------------------------------$"

  private val MINIMUM_NUMBER_OF_ROWS = 4
  private val HEADER_INDEX = 1
  private val COMMENT_START_INDEX = 3

  def parse(comments: String): Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]] = {
    comments
      .split(separator)
      .filterNot(_.isEmpty)
      .filterNot(_ == "\r\n")
      .map { comment =>
          val parsedLines = comment.split("\r\n").toIndexedSeq
          val header = parsedLines(HEADER_INDEX)
          val pattern = """(\d+)?: (.+? .+?) (.+)""".r

          header match {
            case pattern(id, userString, createdAtString) =>
              (for {
                createdAt <- ZonedDateTimeParser.toZonedDateTime(createdAtString)
              } yield {
                val body = if (parsedLines.length < MINIMUM_NUMBER_OF_ROWS) {
                  Seq("")
                } else {
                  parsedLines.view(COMMENT_START_INDEX, parsedLines.length)
                }
                CybozuCSVComment(
                  id = id.toLong,
                  creator = CybozuCSVUser(userString),
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
      }
  }

  def sequence(comments: Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]]): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]] =
    comments.foldRight(Right(Nil): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]]) { (elem, acc) =>
      acc.right.flatMap(list => elem.right.map(a => a +: list))
    }

}
