package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuComment
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class CommentTable(tag: Tag) extends BaseTable[DBCybozuComment](tag, "cybozu_comments") {

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.Unique)
  def parentId: Rep[String] = column[String]("parent_id")
  def creatorId: Rep[String] = column[String]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def content: Rep[String] = column[String]("content")

  override def * : ProvenShape[DBCybozuComment] =
    (id, parentId, creatorId, createdAt, content) <> (DBCybozuComment.tupled, DBCybozuComment.unapply)
}
