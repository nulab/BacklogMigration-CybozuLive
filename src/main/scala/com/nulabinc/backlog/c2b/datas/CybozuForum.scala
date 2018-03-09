package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class CybozuForum(
  id: String,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: DateTime,
  updater: CybozuUser,
  updatedAt: DateTime,
  comments: Seq[CybozuComment]
)

object CybozuForum {
  val fieldSize = 8
}
