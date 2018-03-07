package com.nulabinc.backlog.c2b.parsers

import java.time.{ZoneId, ZonedDateTime}

object ZonedDateTimeParser {

  def toZonedDateTime(value: String): Either[ParseError[ZonedDateTime], ZonedDateTime] = {
    val pattern = """(\d+?)/(\d+?)/(\d+?) .*?(\d+?):(\d+?)""".r
    value match {
      case pattern(year, month, day, hour, minutes) =>
        Right(
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
      case _ => Left(CannotParseFromString(classOf[ZonedDateTime], value))
    }
  }

  def toZonedDate(value: String): Either[ParseError[ZonedDateTime], ZonedDateTime] = {
    val pattern = """(\d+?)/(\d+?)/(\d+?)""".r
    value match {
      case pattern(year, month, day) =>
        Right(
          ZonedDateTime.of(
            year.toInt,
            month.toInt,
            day.toInt,
            0,
            0,
            0,
            0,
            ZoneId.systemDefault()
          )
        )
      case _ => Left(CannotParseFromString(classOf[ZonedDateTime], value))
    }
  }

  def toMaybeZonedDate(value: String): Either[ParseError[ZonedDateTime], Option[ZonedDateTime]] = {
    if (value.isEmpty) {
      Right(None)
    } else {
      toZonedDate(value) match {
        case Right(data) => Right(Some(data))
        case Left(error) => Left(error)
      }
    }
  }
}
