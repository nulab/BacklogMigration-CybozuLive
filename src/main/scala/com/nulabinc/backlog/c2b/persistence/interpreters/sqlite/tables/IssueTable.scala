package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import java.time.ZonedDateTime

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuIssue
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class IssueTable(tag: Tag) extends BaseTable[DBCybozuIssue](tag, "cybozu_issues") {

  def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Unique)
  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creatorId: Rep[String] = column[String]("creator_id")
  def createdAt: Rep[ZonedDateTime] = column[ZonedDateTime]("created_at")
  def updaterId: Rep[String] = column[String]("updater_id")
  def updatedAt: Rep[ZonedDateTime] = column[ZonedDateTime]("updated_at")
  def statusId: Rep[String] = column[String]("status_id")
  def priorityId: Rep[String] = column[String]("priority_id")
  def assigneeId: Rep[Option[String]] = column[Option[String]]("assignee_id")
  def dueDate: Rep[Option[ZonedDateTime]] = column[Option[ZonedDateTime]]("due_date")

  override def * : ProvenShape[DBCybozuIssue] =
    (id, title, content, creatorId, createdAt, updaterId, updatedAt,
      statusId, priorityId, assigneeId, dueDate) <> (DBCybozuIssue.tupled, DBCybozuIssue.unapply)

}
