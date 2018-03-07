package com.nulabinc.backlog.c2b.core.domain.model

import java.time.ZonedDateTime

case class CybozuComment(
  id: Long,
  creator: CybozuUser,
  createdAt: ZonedDateTime,
  content: String
)