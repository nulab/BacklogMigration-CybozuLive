package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuEvent
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.EventTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class EventTableOps() extends BaseTableOps[DBCybozuEvent, EventTable] {

  protected def tableQuery = TableQuery[EventTable]

  def select(id: Int): DBIORead[Option[DBCybozuEvent]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def save(event: DBCybozuEvent): DBIOWrite[DBCybozuEvent] =
    tableQuery
      .filter(_.id === event.id)
      .insertOrUpdate(event)
      .transactionally

}
