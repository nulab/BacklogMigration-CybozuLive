package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.BacklogPriority
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrites
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.BacklogPriorityTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class BacklogPriorityTableOps()(implicit exc: Scheduler) extends BaseTableOps[BacklogPriority, BacklogPriorityTable] {

  protected val tableQuery = TableQuery[BacklogPriorityTable]

  def save(priorities: Seq[BacklogPriority]): DBIOWrites =
    DBIO.sequence(priorities.map { current =>
      tableQuery.insertOrUpdate(current)
    })

}
