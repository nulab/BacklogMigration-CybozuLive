package com.nulabinc.backlog.c2b.persistence.datas

import java.time.ZonedDateTime

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
)

case class DBCybozuUser(fullName: String)