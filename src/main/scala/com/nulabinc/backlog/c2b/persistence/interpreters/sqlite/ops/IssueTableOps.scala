package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuIssue
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.IssueTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class IssueTableOps() extends BaseTableOps[CybozuIssue, IssueTable] {

  protected def tableQuery = TableQuery[IssueTable]

  def save(issue: CybozuIssue): DBIOWrite[CybozuIssue] =
    tableQuery
      .filter(_.id === issue.id)
      .insertOrUpdate(issue)
      .transactionally

}
