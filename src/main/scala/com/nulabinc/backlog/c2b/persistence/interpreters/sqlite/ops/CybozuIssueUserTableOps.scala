package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CybozuIssueUserTable
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

private[sqlite] case class CybozuIssueUserTableOps() {

  private val tableQuery = TableQuery[CybozuIssueUserTable]

  lazy val createTable = tableQuery.schema.create

}
