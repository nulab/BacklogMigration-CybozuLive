package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuDBChat
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._


private[sqlite] class ChatTable(tag: Tag) extends BaseTable[CybozuDBChat](tag, "cybozu_chats") {

  def title: Rep[String] = column[String]("title")
  def description: Rep[String] = column[String]("description")

  override def * : ProvenShape[CybozuDBChat] =
    (id, title, description) <> (CybozuDBChat.tupled, CybozuDBChat.unapply)

}