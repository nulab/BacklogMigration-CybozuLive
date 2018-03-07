package com.nulabinc.backlog.c2b.core.domain.parser

import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser

import scala.util.matching.Regex

object UserParser {

  val pattern: Regex = """(.+?) (.+?)""".r

  def toUser(value: String): Either[ParseError[CybozuUser], CybozuUser] = {
    value match {
      case pattern(firstName, lastName) =>
        Right(
          CybozuUser(
            lastName = lastName,
            firstName = firstName
          )
        )
      case _ => Left(CannotParseFromString(classOf[CybozuUser], value))
    }
  }
}
