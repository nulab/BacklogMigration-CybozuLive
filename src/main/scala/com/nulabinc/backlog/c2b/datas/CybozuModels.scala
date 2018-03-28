package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types._

case class CybozuStatus(value: String) extends AnyVal
case class CybozuPriority(value: String) extends AnyVal

case class CybozuUser(
  id: AnyId,
  userId: String
) extends Entity

object CybozuUser {

  val tupled = (this.apply _).tupled

  def from(user: CybozuCSVUser): CybozuUser =
    new CybozuUser(0, user.value)
}

case class CybozuTodo(
  id: AnyId,
  title: String,
  content: String,
  creator: AnyId,
  createdAt: DateTime,
  updater: AnyId,
  updatedAt: DateTime,
  status: CybozuStatus,
  priority: CybozuPriority,
  dueDate: Option[DateTime]
) extends Entity

object CybozuTodo {

  val tupled = (this.apply _).tupled

  def from(todo: CybozuCSVTodo, creatorId: AnyId, updaterId: AnyId): CybozuTodo =
    new CybozuTodo(
      id = 0,
      title = todo.title,
      content = todo.content,
      creator = creatorId,
      createdAt = todo.createdAt,
      updater = updaterId,
      updatedAt = todo.updatedAt,
      status = CybozuStatus(todo.status.value),
      priority = CybozuPriority(todo.priority.value),
      dueDate = todo.dueDate
    )
}

case class CybozuComment(
  id: AnyId,
  parentId: AnyId,
  creator: AnyId,
  createdAt: DateTime,
  content: String
) extends Entity

object CybozuComment {

  val tupled = (this.apply _).tupled

  def from(parentIssueId: AnyId, comment: CybozuCSVComment, creatorId: AnyId): CybozuComment =
    new CybozuComment(
      id = 0,
      parentId = parentIssueId,
      creator = creatorId,
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
  creator: AnyId
) extends Entity

object CybozuEvent {

  val tupled = (this.apply _).tupled

  def from(event: CybozuCSVEvent, creatorId: AnyId): CybozuEvent =
    new CybozuEvent(
      id = 0,
      startDateTime = event.startDateTime,
      endDateTime = event.endDateTime,
      menu = event.menu,
      title = event.title,
      memo = event.memo,
      creator = creatorId
    )
}

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creator: AnyId,
  createdAt: DateTime,
  updater: AnyId,
  updatedAt: DateTime
) extends Entity

object CybozuForum {

  val tupled = (this.apply _).tupled

  def from(forum: CybozuCSVForum, creatorId: AnyId, updaterId: AnyId): CybozuForum =
    new CybozuForum(
      id = 0,
      title = forum.title,
      content = forum.content,
      creator = creatorId,
      createdAt = forum.createdAt,
      updater = updaterId,
      updatedAt = forum.updatedAt
    )
}