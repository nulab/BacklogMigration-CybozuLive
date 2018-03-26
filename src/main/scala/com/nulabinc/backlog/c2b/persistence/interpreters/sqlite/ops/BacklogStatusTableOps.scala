package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.BacklogStatus
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrites
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.BacklogStatusTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class BacklogStatusTableOps()(implicit exc: Scheduler) extends BaseTableOps[BacklogStatus, BacklogStatusTable] {

  protected val tableQuery = TableQuery[BacklogStatusTable]

  def save(statuses: Seq[BacklogStatus]): DBIOWrites =
    DBIO.sequence(statuses.map { current =>
      tableQuery.insertOrUpdate(current)
    })

}
