package com.nulabinc.backlog.c2b.datas

case class CybozuUser(lastName: String, firstName: String) {

  val key: String = s"$firstName $lastName"
}

object CybozuUser {

  val fieldSize = 3

  def fromFullName(fullName: String): Option[CybozuUser] = {
    val pattern = """(.+?) (.+?)""".r
    fullName match {
      case pattern(firstName, lastName) =>
        Some(
          CybozuUser(
            firstName = firstName,
            lastName = lastName
          )
        )
      case _ => None
    }
  }
}
