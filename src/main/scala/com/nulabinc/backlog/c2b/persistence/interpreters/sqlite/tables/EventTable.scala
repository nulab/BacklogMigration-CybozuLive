package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuDBEvent, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class EventTable(tag: Tag) extends BaseTable[CybozuDBEvent](tag, "cybozu_events") {

  import JdbcMapper._

  def startDateTime: Rep[DateTime] = column[DateTime]("start_date_time")
  def endDateTime: Rep[DateTime] = column[DateTime]("end_date_time")
  def menu: Rep[String] = column[String]("menu")
  def title: Rep[String] = column[String]("title")
  def memo: Rep[String] = column[String]("memo")
  def creator: Rep[AnyId] = column[AnyId]("creator_id")

  override def * :ProvenShape[CybozuDBEvent] =
    (id, startDateTime, endDateTime, menu, title,
      memo, creator) <> (CybozuDBEvent.tupled, CybozuDBEvent.unapply)

}
