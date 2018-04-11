package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}

case class CybozuTodo(
  todo: CybozuDBTodo,
  comments: Seq[CybozuComment],
  creator: CybozuDBUser,
  updater: CybozuDBUser,
  assignees: Seq[CybozuDBUser]
)

case class CybozuEvent(
  id: AnyId,
  startDateTime: DateTime,
  endDateTime: DateTime,
  menu: String,
  title: String,
  memo: String,
  creator: CybozuDBUser,
  comments: Seq[CybozuComment],
)

case class CybozuForum(
  id: AnyId,
  title: String,
  content: String,
  creator: CybozuDBUser,
  createdAt: DateTime,
  updater: CybozuDBUser,
  updatedAt: DateTime,
  comments: Seq[CybozuComment]
)

case class CybozuComment(
  comment: CybozuDBComment,
  creator: CybozuDBUser
)

case class CybozuIssueType(value: String) extends AnyVal
