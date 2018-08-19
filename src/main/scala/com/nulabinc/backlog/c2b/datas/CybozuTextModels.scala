package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.DateTime

case class CybozuTextTopic(
  title: String,
  description: String,
  posts: Seq[CybozuTextPost]
)

case class CybozuTextPost(
  content: String,
  postUser: CybozuTextUser,
  postedAt: DateTime
)

case class CybozuTextUser(value: String) extends AnyVal