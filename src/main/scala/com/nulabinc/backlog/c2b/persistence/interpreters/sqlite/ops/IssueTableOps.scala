package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuIssue
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{IssueTable, JdbcMapper}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class IssueTableOps() extends BaseTableOps[CybozuIssue, IssueTable] {

  import JdbcMapper._

  protected def tableQuery = TableQuery[IssueTable]

  lazy val distinctPriorities =
    tableQuery
      .map(_.priority)
      .distinct
      .result

  def save(issue: CybozuIssue): DBIOWrite =
    tableQuery
      .insertOrUpdate(issue)
      .transactionally

}
