package com.nulabinc.backlog.c2b.datas

case class CybozuUser(
  id: String,
  lastName: String,
  firstName: String
)

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

  def apply(lastName: String, firstName: String): CybozuUser =
    new CybozuUser(s"$firstName $lastName", lastName, firstName)

}
