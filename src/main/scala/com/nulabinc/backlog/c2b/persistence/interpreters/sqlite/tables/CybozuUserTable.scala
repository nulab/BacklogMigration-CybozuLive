package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuUser
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

private[sqlite] class CybozuUserTable(tag: Tag) extends BaseTable[CybozuUser](tag, "cybozu_users") {

  def userId: Rep[String] = column[String]("user_id")

  override def * : ProvenShape[CybozuUser] =
    (id, userId) <> (CybozuUser.tupled, CybozuUser.unapply)
}
