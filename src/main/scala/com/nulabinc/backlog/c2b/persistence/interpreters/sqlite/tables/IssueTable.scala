package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class IssueTable(tag: Tag) extends BaseTable[CybozuIssue](tag, "cybozu_issues") {

  import JdbcMapper._

  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creator: Rep[AnyId] = column[AnyId]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updater: Rep[AnyId] = column[AnyId]("updater_id")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")
  def status: Rep[CybozuStatus] = column[CybozuStatus]("status")
  def priority: Rep[CybozuPriority] = column[CybozuPriority]("priority")
  def dueDate: Rep[Option[DateTime]] = column[Option[DateTime]]("due_date")

  override def * : ProvenShape[CybozuIssue] =
    (id, title, content, creator, createdAt, updater, updatedAt,
      status, priority, dueDate) <> (CybozuIssue.tupled, CybozuIssue.unapply)

}
