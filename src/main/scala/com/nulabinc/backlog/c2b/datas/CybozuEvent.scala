package com.nulabinc.backlog.c2b.datas

import java.time.ZonedDateTime

case class ScheduledMenu(value: String) extends AnyVal

case class CybozuEvent(
  startDateTime: ZonedDateTime,
  endDateTime: ZonedDateTime,
  menu: ScheduledMenu,
  title: String,
  memo: String,
  creator: CybozuUser,
  comments: Seq[CybozuComment]
)

object CybozuEvent {
  val startDateFieldIndex  = 0
  val startTimeFieldIndex  = 1
  val endDateFieldIndex    = 2
  val endTimeFieldIndex    = 3
  val menuFieldIndex       = 4
  val titleFieldIndex      = 5
  val memoFieldIndex       = 6
  val creatorFieldIndex    = 7
  val commentFieldIndex    = 8
  val csvFieldSize         = 9
}