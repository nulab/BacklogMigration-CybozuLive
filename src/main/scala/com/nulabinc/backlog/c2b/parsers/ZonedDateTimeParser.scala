package com.nulabinc.backlog.c2b.parsers

import java.time.{ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.Types.DateTime

object ZonedDateTimeParser {

  def toZonedDateTime(value: String): Either[ParseError[DateTime], DateTime] = {
    val pattern1 = """(\d+?)/(\d+?)/(\d+?) .*?(\d+?):(\d+?):(\d+)""".r
    val pattern2 = """(\d+?)/(\d+?)/(\d+?) .*?(\d+?):(\d+?)""".r
    value match {
      case pattern1(year, month, day, hour, minutes, seconds) =>
        Right(
          ZonedDateTime.of(
            year.toInt,
            month.toInt,
            day.toInt,
            hour.toInt,
            minutes.toInt,
            seconds.toInt,
            0,
            ZoneId.systemDefault()
          )
        )
      case pattern2(year, month, day, hour, minutes) =>
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
      case _ => Left(CannotParseFromString(classOf[DateTime], value))
    }
  }

  def toZonedDateTime(date: String, time: String): Either[ParseError[DateTime], DateTime] = {
    val timeString = if (time.nonEmpty) time else "00:00:00"
    toZonedDateTime(s"$date $timeString")
  }

  def toZonedDate(value: String): Either[ParseError[DateTime], DateTime] = {
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
      case _ => Left(CannotParseFromString(classOf[DateTime], value))
    }
  }

  def toMaybeZonedDate(value: String): Either[ParseError[DateTime], Option[DateTime]] = {
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
