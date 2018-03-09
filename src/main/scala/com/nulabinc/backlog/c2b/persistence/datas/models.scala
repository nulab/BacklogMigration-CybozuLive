package com.nulabinc.backlog.c2b.persistence.datas

import java.time.ZonedDateTime

import com.nulabinc.backlog.c2b.datas.CybozuUser

trait CybozuEntity

case class DBCybozuIssue(
  id: String,
  title: String,
  content: String,
  creatorId: String,
  createdAt: ZonedDateTime,
  updaterId: String,
  updatedAt: ZonedDateTime,
  status: String,
  priority: String,
  assigneeId: Option[String],
  dueDate: Option[ZonedDateTime]
) extends CybozuEntity