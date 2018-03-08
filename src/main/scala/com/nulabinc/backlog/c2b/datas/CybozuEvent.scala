package com.nulabinc.backlog.c2b.datas

import java.time.ZonedDateTime

case class CybozuEvent(
  startDateTime: ZonedDateTime,
  endDateTime: ZonedDateTime,
  menu: ScheduledMenu,
  title: String,
  memo: String,
  creator: CybozuUser,
  comment: String
)

case class ScheduledMenu(value: String) extends AnyVal
