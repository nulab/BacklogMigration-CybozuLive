package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types._

case class Id[T] private(value: AnyId) extends AnyVal

object Id {
  def userId(id: AnyId): Id[CybozuUser] = Id[CybozuUser](id)
}

trait Entity {
  def id: AnyId
}

case class CybozuStatus(value: String) extends AnyVal
case class CybozuPriority(value: String) extends AnyVal
case class CybozuUser(value: String) extends AnyVal

case class CybozuIssue(
  id: AnyId,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime,
  status: CybozuStatus,
  priority: CybozuPriority,
  assignee: Option[CybozuUser],
  dueDate: Option[DateTime]
) extends Entity

object CybozuIssue {

  val tupled = (this.apply _).tupled

  def from(issue: CybozuCSVIssue): CybozuIssue =
    new CybozuIssue(
      0, issue.title, issue.content, CybozuUser(issue.creator.value), issue.createdAt,
      CybozuUser(issue.updater.value), issue.updatedAt, CybozuStatus(issue.status.value),
      CybozuPriority(issue.priority.value), issue.assignee.map(u => CybozuUser(u.value)), issue.dueDate
    )
}

case class CybozuComment(
  id: AnyId,
  parentId: AnyId,
  creatorId: AnyId,
  createdAt: DateTime,
  content: String
) extends Entity

case class CybozuEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creatorId: AnyId
) extends Entity

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creatorId: AnyId,
  createdAt: DateTime,
  updaterId: AnyId,
  updatedAt: DateTime
) extends Entity