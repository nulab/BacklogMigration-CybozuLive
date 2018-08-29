package com.nulabinc.backlog.c2b.parsers

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest._

class ZonedDateTimeParserSpec extends FlatSpec with Matchers {

  "ZonedDateTimeParser" should "parse date time string" in {
    val str = "2019/4/16 06:11:12"
    val actual = ZonedDateTimeParser.toZonedDateTime(str)
    actual shouldEqual Right(ZonedDateTime.of(2019, 4, 16, 6, 11, 12, 0, ZoneId.systemDefault()))
  }

  "ZonedDateTimeParser" should "parse date time string with week" in {
    val str = "2020/2/19 (月) 11:08:01"
    val actual = ZonedDateTimeParser.toZonedDateTime(str)
    actual shouldEqual Right(ZonedDateTime.of(2020, 2, 19, 11, 8, 1, 0, ZoneId.systemDefault()))
  }

  "ZonedDateTimeParser" should "parse date time string without seconds" in {
    val str = "2018/12/28 09:38"
    val actual = ZonedDateTimeParser.toZonedDateTime(str)
    actual shouldEqual Right(ZonedDateTime.of(2018, 12, 28, 9, 38, 0, 0, ZoneId.systemDefault()))
  }

  "ZonedDateTimeParser" should "parse separated date time(empty) string" in {
    val date = "2018/12/28"
    val time = ""
    val actual = ZonedDateTimeParser.toZonedDateTime(date, time)
    actual shouldEqual Right(ZonedDateTime.of(2018, 12, 28, 0, 0, 0, 0, ZoneId.systemDefault()))
  }

  "ZonedDateTimeParser" should "parse separated date time string" in {
    val date = "2015/10/5"
    val time = "08:23"
    val actual = ZonedDateTimeParser.toZonedDateTime(date, time)
    actual shouldEqual Right(ZonedDateTime.of(2015, 10, 5, 8, 23, 0, 0, ZoneId.systemDefault()))
  }

  "ZonedDateTimeParser" should "parse other formatted date time string" in {
    val date = "Fri, Jul 13, 2018 8:36"
    val actual = ZonedDateTimeParser.toZonedDateTime(date)
    actual shouldEqual Right(ZonedDateTime.of(2018, 7, 13, 8, 36, 0, 0, ZoneId.systemDefault()))
  }
}
