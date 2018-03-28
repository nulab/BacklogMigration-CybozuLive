package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class CybozuCSVTodo(
  id: String,
  title: String,
  content: String,
  creator: CybozuCSVUser,
  createdAt: DateTime,
  updater: CybozuCSVUser,
  updatedAt: DateTime,
  status: CybozuCSVStatus,
  priority: CybozuCSVPriority,
  assignees: Seq[CybozuCSVUser],
  dueDate: Option[DateTime],
  comments: Seq[CybozuCSVComment]
)

object CybozuCSVTodo {
  val fieldSize = 12
}

case class CybozuCSVComment(
  id: Long,
  creator: CybozuCSVUser,
  createdAt: DateTime,
  content: String
)

case class CybozuCSVEvent(
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creator: CybozuCSVUser,
  comments: Seq[CybozuCSVComment]
)

object CybozuCSVEvent {
  val fieldSize = 9
}

case class CybozuCSVForum(
  id: String,
  title: String,
  content: String,
  creator: CybozuCSVUser,
  createdAt: DateTime,
  updater: CybozuCSVUser,
  updatedAt: DateTime,
  comments: Seq[CybozuCSVComment]
)

object CybozuCSVForum {
  val fieldSize = 8
}

case class CybozuCSVPriority(value: String) extends AnyVal

case class CybozuCSVStatus(value: String) extends AnyVal

case class CybozuCSVUser(value: String) extends AnyVal