package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{CybozuIssueUser, CybozuIssueUserTable}
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

private[sqlite] case class CybozuIssueUserTableOps() {

  private val tableQuery = TableQuery[CybozuIssueUserTable]

  lazy val createTable = tableQuery.schema.create

  def read(issueId: AnyId): DBIO[Seq[CybozuIssueUser]] =
    tableQuery.filter(_.issueId === issueId).result

  def write(issueId: AnyId, userId: AnyId): DBIOWrite =
    tableQuery += CybozuIssueUser(issueId, userId)

  def write(issueId: AnyId, userIds: Seq[AnyId])(implicit exc: ExecutionContext): DBIOWrite =
    DBIO.sequence(
      userIds.map(userId => write(issueId, userId))
    ).map(r => r.length)
}
