package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuTodo
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{TodoTable, JdbcMapper}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class TodoTableOps() extends BaseTableOps[CybozuTodo, TodoTable] {

  import JdbcMapper._

  protected def tableQuery = TableQuery[TodoTable]

  lazy val distinctPriorities =
    tableQuery
      .map(_.priority)
      .distinct
      .result

}
