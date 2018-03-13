package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuIssue, CybozuStatus}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class IssueTable(tag: Tag) extends BaseTable[CybozuIssue](tag, "cybozu_issues") {

  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creatorId: Rep[AnyId] = column[AnyId]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updaterId: Rep[AnyId] = column[AnyId]("updater_id")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")
  def status: Rep[CybozuStatus] = column[CybozuStatus]("status")
  def priority: Rep[String] = column[String]("priority")
  def assigneeId: Rep[Option[AnyId]] = column[Option[AnyId]]("assignee_id")
  def dueDate: Rep[Option[DateTime]] = column[Option[DateTime]]("due_date")

  override def * : ProvenShape[CybozuIssue] =
    (id, title, content, creatorId, createdAt, updaterId, updatedAt,
      status, priority, assigneeId, dueDate) <> (CybozuIssue.tupled, CybozuIssue.unapply)

}
