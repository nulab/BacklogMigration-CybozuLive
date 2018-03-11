package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class ScheduledMenu(value: String) extends AnyVal

case class CybozuEvent(
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: ScheduledMenu,
  title: String,
  memo: String,
  creator: CybozuUser,
  comments: Seq[CybozuComment]
)

object CybozuEvent {
  val fieldSize = 9
}