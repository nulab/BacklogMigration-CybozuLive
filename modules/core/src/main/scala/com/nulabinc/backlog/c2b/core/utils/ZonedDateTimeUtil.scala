package com.nulabinc.backlog.c2b.core.utils

import java.time.{ZoneId, ZonedDateTime}

object ZonedDateTimeUtil {

  def toZonedDateTime(value: String): Option[ZonedDateTime] = {
    val pattern = """(\d+?)/(\d+?)/(\d+?) .+? (\d+?):(\d+?)""".r
    value match {
      case pattern(year, month, day, hour, minutes) =>
        Some(
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
      case _ => None
    }
  }
}
