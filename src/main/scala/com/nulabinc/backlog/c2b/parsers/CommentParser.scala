package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.CybozuCSVComment

object CommentParser {

  val separator: String = "--------------------------------------------------"

  def parse(comments: String): Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]] = {

    val MINIMUM_NUMBER_OF_ROWS = 5
    val HEADER_INDEX = 1
    val COMMENT_START_INDEX = 3
    
    comments
      .split(separator)
      .filterNot(_.isEmpty)
      .filterNot(_ == "\n")
      .map { comment =>
          val parsedLines = comment.split("\n").toIndexedSeq
          if (parsedLines.length < MINIMUM_NUMBER_OF_ROWS) {
            Left(CannotParseComment("Invalid line numbers.", parsedLines.mkString("\n")))
          } else {
            val header = parsedLines(HEADER_INDEX)
            val body = parsedLines.view(COMMENT_START_INDEX, parsedLines.length)
            val pattern = """(\d+)?: (.+? .+?) (.+)""".r

            header match {
              case pattern(id, userString, createdAtString) =>
                (for {
                  user      <- UserParser.toUser(userString)
                  createdAt <- ZonedDateTimeParser.toZonedDateTime(createdAtString)
                } yield {
                  CybozuCSVComment(
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
          }

      }
  }

  def sequence(comments: Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]]): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]] =
    comments.foldRight(Right(Nil): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]]) { (elem, acc) =>
      acc.right.flatMap(list => elem.right.map(a => a +: list))
    }
}
