package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class CybozuIssue(
  id: String,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime,
  status: CybozuStatus,
  priority: CybozuPriority,
  assignee: Option[CybozuUser],
  dueDate: Option[DateTime],
  comments: Seq[CybozuComment]
)

object CybozuIssue {
  val fieldSize = 12
}