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
  creator: CybozuUser,
  createdAt: DateTime,
  content: String
) extends Entity

object CybozuComment {

  val tupled = (this.apply _).tupled

  def from(parentIssueId: AnyId, comment: CybozuCSVComment): CybozuComment =
    new CybozuComment(
      id = 0,
      parentId = parentIssueId,
      creator = CybozuUser(comment.creator.value),
      createdAt = comment.createdAt,
      content = comment.content
    )
}

case class CybozuEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creator: CybozuUser
) extends Entity

object CybozuEvent {

  val tupled = (this.apply _).tupled

  def from(event: CybozuCSVEvent): CybozuEvent =
    new CybozuEvent(
      id = 0,
      startDateTime = event.startDateTime,
      endDateTime = event.endDateTime,
      menu = event.menu,
      title = event.title,
      memo = event.memo,
      creator = CybozuUser(event.creator.value)
    )
}

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime
) extends Entity

object CybozuForum {

  val tupled = (this.apply _).tupled

  def from(forum: CybozuCSVForum): CybozuForum =
    new CybozuForum(
      id = 0,
      title = forum.title,
      content = forum.content,
      creator = CybozuUser(forum.creator.value),
      createdAt = forum.createdAt,
      updater = CybozuUser(forum.updater.value),
      updatedAt = forum.updatedAt
    )
}