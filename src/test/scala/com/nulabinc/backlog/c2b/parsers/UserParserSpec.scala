package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.domains.CybozuUser
import org.scalatest.{FlatSpec, Matchers}

class UserParserSpec extends FlatSpec with Matchers {

  "UserParser" should "parse string" in {
    val str = "Shoma Nishi"
    val actual = UserParser.toUser(str)
    val expect = CybozuUser(firstName = "Shoma", lastName = "Nishi")
    actual shouldEqual Right(expect)
  }
}
