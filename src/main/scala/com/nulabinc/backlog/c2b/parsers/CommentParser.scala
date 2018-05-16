package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.{CybozuCSVComment, CybozuCSVUser}

object CommentParser {

  private val commentPattern = """(?ms)\r\n(\d+)?: (.+? .+?) (.+?)\r\n\r\n(.*?)\r\n\r\n--------------------------------------------------""".r

  def parse(comments: String): Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]] =
    commentPattern
      .findAllIn(comments)
      .map {
        case commentPattern(id, userString, createdAtString, body) =>
          (for {
            createdAt <- ZonedDateTimeParser.toZonedDateTime(createdAtString)
          } yield {
            CybozuCSVComment(
              id = id.toLong,
              creator = CybozuCSVUser(userString),
              createdAt = createdAt,
              content = body
            )
          }) match {
            case Right(a) => Right(a)
            case Left(error) =>
              Left(CannotParseComment("Header parsing error.", error.toString))
          }
        case other => Left(CannotParseComment("Invalid header", other))
      }.toSeq

  def sequence(comments: Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]]): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]] =
    comments.foldRight(Right(Nil): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]]) { (elem, acc) =>
      acc.right.flatMap(list => elem.right.map(a => a +: list))
    }

}
