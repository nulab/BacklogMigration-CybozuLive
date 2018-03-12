package com.nulabinc.backlog.c2b.persistence.datas

import java.time.ZonedDateTime

import com.nulabinc.backlog.c2b.datas.Types.DateTime

trait Entity

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
  id: Long,
  parentId: String,
  creatorId: String,
  createdAt: ZonedDateTime,
  content: String
) extends Entity

case class DBCybozuUser(
  id: String,
  firstName: String,
  lastName: String
) extends Entity

case class DBCybozuEvent(
  id: Int,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creatorId: String
) extends Entity

case class DBCybozuForum(
  id: String,
  title: String,
  content: String,
  creatorId: String,
  createdAt: DateTime,
  updaterId: String,
  updatedAt: DateTime
) extends Entity