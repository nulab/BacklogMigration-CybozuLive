package com.nulabinc.backlog.c2b.domains

case class CybozuUser(
  lastName: String,
  firstName: String
) {

  val key: String = s"$firstName $lastName"

}

object CybozuUser {
  val fieldSize = 3
}
