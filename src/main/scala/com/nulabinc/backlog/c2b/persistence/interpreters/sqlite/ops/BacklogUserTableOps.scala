package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.BacklogUser
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.BacklogUserTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class BacklogUserTableOps()(implicit exc: Scheduler) extends BaseTableOps[BacklogUser, BacklogUserTable] {

  protected val tableQuery = TableQuery[BacklogUserTable]

  def save(user: BacklogUser): DBIOWrite =
    tableQuery
      .returning(tableQuery.map(_.id))
      .insertOrUpdate(user)

}
