package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}

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
  comments: Seq[CybozuComment]
)

case class CybozuComment(
  id: AnyId,
  parentId: AnyId,
  creator: CybozuUser,
  createdAt: DateTime,
  content: String
)

case class CybozuIssueType(value: String) extends AnyVal
case class CybozuStatus(value: String) extends AnyVal
case class CybozuPriority(value: String) extends AnyVal
