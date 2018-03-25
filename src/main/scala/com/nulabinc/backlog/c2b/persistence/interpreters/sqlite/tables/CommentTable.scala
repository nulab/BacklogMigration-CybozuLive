package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuComment, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class CommentTable(tag: Tag) extends BaseTable[CybozuComment](tag, "cybozu_comments") {

  def parentId: Rep[AnyId] = column[AnyId]("parent_id")
  def creator: Rep[CybozuUser] = column[CybozuUser]("creator")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def content: Rep[String] = column[String]("content")

  override def * : ProvenShape[CybozuComment] =
    (id, parentId, creator, createdAt, content) <> (CybozuComment.tupled, CybozuComment.unapply)
}
