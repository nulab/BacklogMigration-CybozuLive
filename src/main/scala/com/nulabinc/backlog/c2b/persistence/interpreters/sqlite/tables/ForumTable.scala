package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuForum
import com.nulabinc.backlog.c2b.datas.Types.DateTime
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class ForumTable(tag: Tag) extends BaseTable[CybozuForum](tag, "cybozu_forums") {

  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creatorId: Rep[String] = column[String]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updaterId: Rep[String] = column[String]("updater_id")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")

  override def * : ProvenShape[CybozuForum] =
    (id, title, content, creatorId, createdAt,
      updaterId, updatedAt) <> (CybozuForum.tupled, CybozuForum.unapply)

}
