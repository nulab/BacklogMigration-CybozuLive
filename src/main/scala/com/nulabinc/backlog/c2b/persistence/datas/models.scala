package com.nulabinc.backlog.c2b.persistence.datas

import java.time.ZonedDateTime

trait Entity {
  def id: String
}

case class DBCybozuIssue(
  id: String,
  title: String,
  content: String,
  creatorId: String,
  createdAt: ZonedDateTime,
  updaterId: String,
  updatedAt: ZonedDateTime,
  status: String,
  priority: String,
  assigneeId: Option[String],
  dueDate: Option[ZonedDateTime]
) extends Entity

case class DBCybozuComment(
  id: String,
  issueId: String,
  creatorId: String,
  createdAt: ZonedDateTime,
  content: String
) extends Entity

case class DBCybozuUser(
  id: String,
  firstName: String,
  lastName: String
) extends Entity