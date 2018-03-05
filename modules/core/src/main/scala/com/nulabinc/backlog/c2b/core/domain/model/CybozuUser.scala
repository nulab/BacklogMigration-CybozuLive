package com.nulabinc.backlog.c2b.core.domain.model

case class CybozuUser(
  lastName: String,
  firstName: String,
  emailAddress: String
) {

  val key: String = s"$firstName $lastName"

}
