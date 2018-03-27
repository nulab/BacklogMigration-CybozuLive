package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuCSVIssue
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{CybozuIssueUserTable, IssueTable}
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

private[sqlite] case class CybozuIssueUserTableOps() {

  private val tableQuery = TableQuery[CybozuIssueUserTable]

  lazy val createTable = tableQuery.schema.create

}
