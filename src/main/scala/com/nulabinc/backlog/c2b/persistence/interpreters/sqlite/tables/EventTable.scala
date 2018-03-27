package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuEvent, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class EventTable(tag: Tag) extends BaseTable[CybozuEvent](tag, "cybozu_events") {

  import JdbcMapper._

  def startDateTime: Rep[DateTime] = column[DateTime]("start_date_time")
  def endDateTime: Rep[DateTime] = column[DateTime]("end_date_time")
  def menu: Rep[String] = column[String]("menu")
  def title: Rep[String] = column[String]("title")
  def memo: Rep[String] = column[String]("memo")
  def creator: Rep[CybozuUser] = column[CybozuUser]("creator")

  override def * :ProvenShape[CybozuEvent] =
    (id, startDateTime, endDateTime, menu, title,
      memo, creator) <> (CybozuEvent.tupled, CybozuEvent.unapply)

}
