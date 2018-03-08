package com.nulabinc.backlog.c2b.domains

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
  val fieldSize = 9
}