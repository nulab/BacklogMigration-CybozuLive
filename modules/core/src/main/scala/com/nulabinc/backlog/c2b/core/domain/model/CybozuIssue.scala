package com.nulabinc.backlog.c2b.core.domain.model

import java.time.ZonedDateTime

case class CybozuIssue(
  id: String,
  title: String,
  content: String,
  creator: CybozuUser,
  createdAt: ZonedDateTime,
  updater: CybozuUser,
  updatedAt: ZonedDateTime,
  status: CybozuStatus,
  priority: CybozuPriority,
  assignee: Option[CybozuUser],
  dueDate: Option[ZonedDateTime],
  comments: Seq[CybozuComment]
)