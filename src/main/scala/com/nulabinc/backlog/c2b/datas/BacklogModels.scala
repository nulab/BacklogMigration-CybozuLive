package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.AnyId

case class BacklogUser(
  id: AnyId,
  key: Long,
  userId: Option[String],
  name: String,
  emailAddress: String
) extends Entity
