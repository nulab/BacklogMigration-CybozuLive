package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.DateTime
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class IssueTable(tag: Tag) extends BaseTable[CybozuIssue](tag, "cybozu_issues") {

  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creator: Rep[CybozuUser] = column[CybozuUser]("creator")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updater: Rep[CybozuUser] = column[CybozuUser]("updater")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")
  def status: Rep[CybozuStatus] = column[CybozuStatus]("status")
  def priority: Rep[CybozuPriority] = column[CybozuPriority]("priority")
  def assignee: Rep[Option[CybozuUser]] = column[Option[CybozuUser]]("assignee")
  def dueDate: Rep[Option[DateTime]] = column[Option[DateTime]]("due_date")

  override def * : ProvenShape[CybozuIssue] =
    (id, title, content, creator, createdAt, updater, updatedAt,
      status, priority, assignee, dueDate) <> (CybozuIssue.tupled, CybozuIssue.unapply)

}
