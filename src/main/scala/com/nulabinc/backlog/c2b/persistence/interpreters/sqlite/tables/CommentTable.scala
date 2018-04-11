package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuDBComment, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class CommentTable(tag: Tag) extends BaseTable[CybozuDBComment](tag, "cybozu_comments") {

  import JdbcMapper._

  def parentId: Rep[AnyId] = column[AnyId]("parent_id")
  def creator: Rep[AnyId] = column[AnyId]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def content: Rep[String] = column[String]("content")

  override def * : ProvenShape[CybozuDBComment] =
    (id, parentId, creator, createdAt, content) <> (CybozuDBComment.tupled, CybozuDBComment.unapply)
}
