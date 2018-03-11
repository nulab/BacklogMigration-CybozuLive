package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuForum
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class ForumTable(tag: Tag) extends BaseTable[DBCybozuForum](tag, "cybozu_forums") {

  def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Unique)
  def title: Rep[String] = column[String]("title")
  def content: Rep[String] = column[String]("content")
  def creatorId: Rep[String] = column[String]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def updaterId: Rep[String] = column[String]("updater_id")
  def updatedAt: Rep[DateTime] = column[DateTime]("updated_at")

  override def * : ProvenShape[DBCybozuForum] =
    (id, title, content, creatorId, createdAt,
      updaterId, updatedAt) <> (DBCybozuForum.tupled, DBCybozuForum.unapply)

}
