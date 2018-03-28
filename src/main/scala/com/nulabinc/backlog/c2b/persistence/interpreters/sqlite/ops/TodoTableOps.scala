package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.{CybozuPriority, CybozuStatus, CybozuTodo}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOStream
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{JdbcMapper, TodoTable}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class TodoTableOps() extends BaseTableOps[CybozuTodo, TodoTable] {

  import JdbcMapper._

  protected def tableQuery = TableQuery[TodoTable]

  lazy val distinctPriorities: DBIOStream[CybozuPriority] =
    tableQuery
      .map(_.priority)
      .distinct
      .result

  lazy val distinctStatuses: DBIOStream[CybozuStatus] =
    tableQuery
      .map(_.status)
      .distinct
      .result

}
