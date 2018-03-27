package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class CybozuIssueUser(cybozuIssueId: AnyId, cybozuUserId: AnyId)

private[sqlite] class CybozuIssueUserTable(tag: Tag) extends Table[CybozuIssueUser](tag, "cybozu_issues_users") {

  def issueId: Rep[AnyId] = column[AnyId]("cybozu_issue_id")
  def userId: Rep[AnyId] = column[AnyId]("cybozu_user_id")

  override def * : ProvenShape[CybozuIssueUser] =
    (issueId, userId) <> (CybozuIssueUser.tupled, CybozuIssueUser.unapply)

}
