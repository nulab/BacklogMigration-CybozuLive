package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.CybozuCSVUser

import scala.util.matching.Regex

object UserParser {

  val pattern: Regex = """(.+?) (.+?)""".r

  def toUser(value: String): Either[ParseError[CybozuCSVUser], CybozuCSVUser] = {
    value match {
      case pattern(firstName, lastName) =>
        Right(
          CybozuCSVUser(
            lastName = lastName,
            firstName = firstName
          )
        )
      case _ => Left(CannotParseFromString(classOf[CybozuCSVUser], value))
    }
  }

  def toMaybeUser(value: String): Either[ParseError[CybozuCSVUser], Option[CybozuCSVUser]] = {
    if (value.isEmpty) {
      Right(None)
    } else {
      toUser(value) match {
        case Right(data) => Right(Some(data))
        case Left(error) => Left(error)
      }
    }
  }
}
