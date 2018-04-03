package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuDBUser
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

private[sqlite] class CybozuUserTable(tag: Tag) extends BaseTable[CybozuDBUser](tag, "cybozu_users") {

  def userId: Rep[String] = column[String]("user_id")

  override def * : ProvenShape[CybozuDBUser] =
    (id, userId) <> (CybozuDBUser.tupled, CybozuDBUser.unapply)
}
