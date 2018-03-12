package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuIssue
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.IssueTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class IssueTableOps() extends BaseTableOps[DBCybozuIssue, IssueTable] {

  protected def tableQuery = TableQuery[IssueTable]

  def select(id: String): DBIORead[Option[DBCybozuIssue]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def save(forum: DBCybozuIssue): DBIOWrite[DBCybozuIssue] =
    tableQuery
      .filter(_.id === forum.id)
      .insertOrUpdate(forum)
      .transactionally
  
}
