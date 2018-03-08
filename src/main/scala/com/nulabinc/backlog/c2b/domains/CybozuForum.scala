package com.nulabinc.backlog.c2b.domains

import java.time.ZonedDateTime

case class CybozuForum(
  id: String,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: ZonedDateTime,
  updater: CybozuUser,
  updatedAt: ZonedDateTime,
  comments: Seq[CybozuComment]
)

object CybozuForum {
  val fieldSize = 8
}
