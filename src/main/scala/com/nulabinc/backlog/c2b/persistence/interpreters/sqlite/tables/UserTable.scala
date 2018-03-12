package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.CybozuUser
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class UserTable(tag: Tag) extends BaseTable[CybozuUser](tag, "cybozu_users") {

  def firstName: Rep[String] = column[String]("first_name")
  def lastName: Rep[String] = column[String]("last_name")

  override def * : ProvenShape[CybozuUser] =
    (id, firstName, lastName) <> (CybozuUser.tupled, CybozuUser.unapply)

}
