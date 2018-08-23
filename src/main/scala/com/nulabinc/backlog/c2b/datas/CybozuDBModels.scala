package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types._

case class CybozuDBTodo(
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

object CybozuDBTodo {

  val tupled = (this.apply _).tupled

  def from(todo: CybozuCSVTodo, creatorId: AnyId, updaterId: AnyId): CybozuDBTodo =
    new CybozuDBTodo(
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

case class CybozuDBComment(
  id: AnyId,
  parentId: AnyId,
  creator: AnyId,
  createdAt: DateTime,
  content: String
) extends Entity

object CybozuDBComment {

  val tupled = (this.apply _).tupled

  def from(parentIssueId: AnyId, comment: CybozuCSVComment, creatorId: AnyId): CybozuDBComment =
    new CybozuDBComment(
      id = 0,
      parentId = parentIssueId,
      creator = creatorId,
      createdAt = comment.createdAt,
      content = comment.content
    )

  def to(comment: CybozuDBComment, creator: CybozuUser): CybozuComment =
    CybozuComment(
      id = comment.id,
      parentId = comment.parentId,
      creator = creator,
      createdAt = comment.createdAt,
      content = comment.content
    )
}

case class CybozuDBEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creator: AnyId
) extends Entity

object CybozuDBEvent {

  val tupled = (this.apply _).tupled

  def from(event: CybozuCSVEvent, creatorId: AnyId): CybozuDBEvent =
    new CybozuDBEvent(
      id = 0,
      startDateTime = event.startDateTime,
      endDateTime = event.endDateTime,
      menu = event.menu,
      title = event.title,
      memo = event.memo,
      creator = creatorId
    )
}

case class CybozuDBForum(
  id: AnyId,
  title: String,
  content: String,
  creator: AnyId,
  createdAt: DateTime,
  updater: AnyId,
  updatedAt: DateTime
) extends Entity

object CybozuDBForum {

  val tupled = (this.apply _).tupled

  def from(forum: CybozuCSVForum, creatorId: AnyId, updaterId: AnyId): CybozuDBForum =
    new CybozuDBForum(
      id = 0,
      title = forum.title,
      content = forum.content,
      creator = creatorId,
      createdAt = forum.createdAt,
      updater = updaterId,
      updatedAt = forum.updatedAt
    )
}

case class CybozuDBChat(
  id: AnyId,
  title: String,
  description: String
) extends Entity

object CybozuDBChat {

  val tupled = (this.apply _).tupled

  def from(topic: CybozuTextTopic): CybozuDBChat =
    CybozuDBChat(
      id = 0,
      title = topic.title,
      description = topic.description
    )
}