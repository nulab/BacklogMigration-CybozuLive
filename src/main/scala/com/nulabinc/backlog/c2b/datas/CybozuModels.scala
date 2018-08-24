package com.nulabinc.backlog.c2b.datas

import java.time.{ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}

case class CybozuUser(
  id: AnyId,
  userId: String
) extends Entity

object CybozuUser {
  val tupled = (this.apply _).tupled
  def from(user: CybozuCSVUser): CybozuUser =
    CybozuUser(0, user.value)
  def fromCybozuTextUser(user: CybozuTextUser): CybozuUser =
    CybozuUser(0, user.value)
}

case class CybozuTodo(
  id: AnyId,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime,
  status: CybozuStatus,
  priority: CybozuPriority,
  dueDate: Option[DateTime],
  comments: Seq[CybozuComment],
  assignees: Seq[CybozuUser]
)

case class CybozuEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creator: CybozuUser,
  comments: Seq[CybozuComment],
)

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime,
  comments: Seq[CybozuComment]
)

case class CybozuChat(
  id: AnyId,
  title: String,
  description: String,
  comments: Seq[CybozuComment],
  createdAt: DateTime = ZonedDateTime.now(ZoneId.systemDefault)
)

case class CybozuComment(
  id: AnyId,
  parentId: AnyId,
  creator: CybozuUser,
  createdAt: DateTime,
  content: String
)

object CybozuComment {
  def from(parentId: AnyId, post: CybozuTextPost, postUserId: AnyId): CybozuComment =
    CybozuComment(
      id = 0,
      parentId = parentId,
      creator = CybozuUser(postUserId, post.postUser.value),
      createdAt = post.postedAt,
      content = post.content
    )
}

case class CybozuIssueType(value: String) extends AnyVal
case class CybozuStatus(value: String) extends AnyVal
case class CybozuPriority(value: String) extends AnyVal
