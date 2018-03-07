package com.nulabinc.backlog.c2b.datas

case class CybozuUser(
  lastName: String,
  firstName: String
) {

  val key: String = s"$firstName $lastName"

}
