package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.BacklogUser
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] class BacklogUserTable(tag: Tag) extends BaseTable[BacklogUser](tag, "backlog_users") {

  def key: Rep[Long] = column[Long]("key")
  def userId: Rep[Option[String]] = column[Option[String]]("user_id")
  def name: Rep[String] = column[String]("name")
  def emailAddress: Rep[String] = column[String]("email_address")

  override def * : ProvenShape[BacklogUser] =
    (id, key, userId, name, emailAddress) <> (BacklogUser.tupled, BacklogUser.unapply)
}
