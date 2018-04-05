package com.nulabinc.backlog.c2b.datas

import backlog4s.datas.User
import com.nulabinc.backlog.c2b.datas.Types.AnyId

case class BacklogUser(
  id: AnyId,
  key: Long,
  userId: Option[String],
  name: String,
  emailAddress: String
) extends Entity

object BacklogUser{

  val tupled = (this.apply _).tupled

  def from(user: User): BacklogUser =
    new BacklogUser(0, user.id.value, user.userId, user.name, user.mailAddress)
}

case class BacklogPriority(
  id: AnyId,
  name: String
) extends Entity

case class BacklogStatus(
  id: AnyId,
  name: String
) extends Entity
