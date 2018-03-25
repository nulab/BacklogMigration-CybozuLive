package com.nulabinc.backlog.c2b.parsers

import java.time.{ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.CybozuCSVUser
import org.scalatest.{FlatSpec, Matchers}

class CommentParserSpec extends FlatSpec with Matchers {

  "CommentParser" should "parse string" in {
    val result = CommentParser.parse(source)
    val comments = CommentParser.sequence(result)
    val actual = comments.right.get


//    actual(1).creator shouldEqual CybozuCSVUser("Nishitaten", "Shoma")
    actual(1).createdAt shouldEqual ZonedDateTime.of(2018, 3, 7, 10, 44, 0, 0, ZoneId.systemDefault())
    actual(1).content shouldEqual expectComment
  }

  def source = """--------------------------------------------------
                 |2: Shoma Nishitaten 2018/3/7 (水) 10:44
                 |
                 |     a
                 |    aa
                 |   aaaa
                 | aaaaaaa
                 |aaaaaaaaa
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/3/7 (水) 10:44
                 |
                 |123
                 |345
                 |789
                 |
                 |--------------------------------------------------
                 |""".stripMargin

  def expectComment =
    """123
      |345
      |789""".stripMargin
}
