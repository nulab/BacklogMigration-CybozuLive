package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuForum, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class ForumTable(tag: Tag) extends BaseTable[CybozuForum](tag, "cybozu_forums") {

  import JdbcMapper._

  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creator: Rep[AnyId] = column[AnyId]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updater: Rep[AnyId] = column[AnyId]("updater_id")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")

  override def * : ProvenShape[CybozuForum] =
    (id, title, content, creator, createdAt,
      updater, updatedAt) <> (CybozuForum.tupled, CybozuForum.unapply)

}
