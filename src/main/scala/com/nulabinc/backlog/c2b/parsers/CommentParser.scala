package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.c2b.datas.{CybozuCSVComment, CybozuCSVUser}

object CommentParser {

  private val commentWithLeftUserPattern = """(?ms)\r\n(\d+)?: (\(.+?\)) (.+?)\r\n\r\n(.*?)\r\n\r\n--------------------------------------------------""".r
  private val commentPattern = """(?ms)\r\n(\d+)?: (.+? .+?) (.+?)\r\n\r\n(.*?)\r\n\r\n--------------------------------------------------""".r

  def parse(comments: String): Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]] =
    commentPattern
      .findAllIn(comments)
      .map {
        case commentWithLeftUserPattern(id, userString, createdAtString, body) =>
          create(id, userString, createdAtString, body)
        case commentPattern(id, userString, createdAtString, body) =>
          create(id, userString, createdAtString, body)
        case other =>
          Left(CannotParseComment("Invalid header", other))
      }.toSeq

  def sequence(comments: Seq[Either[ParseError[CybozuCSVComment] , CybozuCSVComment]]): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]] =
    comments.foldRight(Right(Nil): Either[ParseError[CybozuCSVComment], Seq[CybozuCSVComment]]) { (elem, acc) =>
      acc.right.flatMap(list => elem.right.map(a => a +: list))
    }

  private def parseDateTime(str: String): Either[ParseError[CybozuCSVComment], DateTime] =
    ZonedDateTimeParser.toZonedDateTime(str) match {
      case Right(value) => Right(value)
      case Left(error) => Left(CannotParseComment("Invalid date time", error.toString))
    }

  private def create(id: String, userStr: String, createdAtStr: String, body: String) =
    parseDateTime(createdAtStr).map { createdAt =>
      CybozuCSVComment(
        id = id.toLong,
        creator = CybozuCSVUser(userStr),
        createdAt = createdAt,
        content = body
      )
    }

}
