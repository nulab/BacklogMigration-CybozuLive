package com.nulabinc.backlog.c2b.datas

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
  val idFieldIndex        = 0
  val titleFieldIndex     = 1
  val contentFieldIndex   = 2
  val creatorFieldIndex   = 3
  val createdAtFieldIndex = 4
  val updaterFieldIndex   = 5
  val updatedAtFieldIndex = 6
  val commentFieldIndex   = 7
  val csvFieldSize        = 8
}
