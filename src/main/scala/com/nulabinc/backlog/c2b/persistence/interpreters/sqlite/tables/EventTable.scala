package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.ScheduledMenu
import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuEvent
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class EventTable(tag: Tag) extends BaseTable[DBCybozuEvent](tag, "cybozu_events") {

  implicit val scheduledMenuMapper: JdbcType[ScheduledMenu] with BaseTypedType[ScheduledMenu] =
    MappedColumnType.base[ScheduledMenu, String](
      menu => menu.value,
      str => ScheduledMenu(str)
    )

  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.Unique, O.AutoInc)
  def startDateTime: Rep[DateTime] = column[DateTime]("start_date_time")
  def endDateTime: Rep[DateTime] = column[DateTime]("end_date_time")
  def menu: Rep[ScheduledMenu] = column[ScheduledMenu]("menu")
  def title: Rep[String] = column[String]("title")
  def memo: Rep[String] = column[String]("memo")
  def creatorId: Rep[String] = column[String]("creator_id")

  override def * :ProvenShape[DBCybozuEvent] =
    (id, startDateTime, endDateTime, menu, title,
      memo, creatorId) <> (DBCybozuEvent.tupled, DBCybozuEvent.unapply)

}
