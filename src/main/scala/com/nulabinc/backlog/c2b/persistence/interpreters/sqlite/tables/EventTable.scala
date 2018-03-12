package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuEvent
import com.nulabinc.backlog.c2b.datas.Types.DateTime
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class EventTable(tag: Tag) extends BaseTable[CybozuEvent](tag, "cybozu_events") {

  def startDateTime: Rep[DateTime] = column[DateTime]("start_date_time")
  def endDateTime: Rep[DateTime] = column[DateTime]("end_date_time")
  def menu: Rep[String] = column[String]("menu")
  def title: Rep[String] = column[String]("title")
  def memo: Rep[String] = column[String]("memo")
  def creatorId: Rep[String] = column[String]("creator_id")

  override def * :ProvenShape[CybozuEvent] =
    (id, startDateTime, endDateTime, menu, title,
      memo, creatorId) <> (CybozuEvent.tupled, CybozuEvent.unapply)

}
