package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class CybozuComment(
  id: Long,
  creator: CybozuUser,
  createdAt: DateTime,
  content: String
)