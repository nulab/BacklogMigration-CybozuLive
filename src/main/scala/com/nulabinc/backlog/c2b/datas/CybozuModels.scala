package com.nulabinc.backlog.c2b.datas

case class CybozuTodo(
  todo: CybozuDBTodo,
  comments: Seq[CybozuComment],
  creator: CybozuDBUser,
  updater: CybozuDBUser,
  assignees: Seq[CybozuDBUser]
)

case class CybozuForum(
  forum: CybozuDBForum,
  comments: Seq[CybozuComment],
  creator: CybozuDBUser,
  updater: CybozuDBUser
)

case class CybozuComment(
  comment: CybozuDBComment,
  creator: CybozuDBUser
)