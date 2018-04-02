package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuDBEvent
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.EventTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class EventTableOps() extends BaseTableOps[CybozuDBEvent, EventTable] {

  protected def tableQuery = TableQuery[EventTable]

}
