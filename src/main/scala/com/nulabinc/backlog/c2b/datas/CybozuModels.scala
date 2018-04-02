package com.nulabinc.backlog.c2b.datas

case class CybozuTodo(
  todo: CybozuDBTodo,
  comments: Seq[CybozuDBComment],
  creator: CybozuDBUser,
  updater: CybozuDBUser,
  assignees: Seq[CybozuDBUser]
)
