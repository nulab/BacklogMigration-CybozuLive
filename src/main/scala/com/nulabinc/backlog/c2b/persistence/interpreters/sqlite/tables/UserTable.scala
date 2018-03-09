package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuUser
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class UserTable(tag: Tag) extends BaseTable[DBCybozuUser](tag, "cybozu_users") {

  def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Unique)

  override def * : ProvenShape[DBCybozuUser] =
    id <> (DBCybozuUser.apply, DBCybozuUser.unapply)

}
