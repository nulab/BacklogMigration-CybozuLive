package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types._

case class Id[T] private(value: AnyId) extends AnyVal

trait Entity {
  def id: AnyId
}

case class CybozuIssue(
  id: AnyId,
  title: String,
  content: String,
  creatorId: String,
  createdAt: DateTime,
  updaterId: String,
  updatedAt: DateTime,
  status: String,
  priority: String,
  assigneeId: Option[String],
  dueDate: Option[DateTime]
) extends Entity

case class CybozuComment(
  id: AnyId,
  parentId: AnyId,
  creatorId: String,
  createdAt: DateTime,
  content: String
) extends Entity

case class CybozuUser(
  id: AnyId,
  firstName: String,
  lastName: String
) extends Entity {
  def key: String = s"$firstName $lastName"
}

case class CybozuEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creatorId: String
) extends Entity

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creatorId: String,
  createdAt: DateTime,
  updaterId: String,
  updatedAt: DateTime
) extends Entity