package com.nulabinc.backlog.c2b.parsers

import java.time.{ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.CybozuCSVUser
import org.scalatest.{FlatSpec, Matchers}

class CommentParserSpec extends FlatSpec with Matchers {

  "CommentParser" should "parse string" in {
    val result = CommentParser.parse(source)
    val comments = CommentParser.sequence(result)
    
    comments.isRight shouldBe true
    comments.map { actual =>
      actual.length shouldEqual 4
      actual(0).id shouldEqual 27
      actual(0).creator shouldEqual CybozuCSVUser("Shoma Nishitaten")
      actual(0).createdAt shouldEqual ZonedDateTime.of(2018, 3, 7, 10, 44, 0, 0, ZoneId.systemDefault())
      actual(0).content shouldEqual "ありがとうございます。\r\n明日やってみます。"
      actual(1).creator shouldEqual CybozuCSVUser("(サービス退会者)")
      actual(1).createdAt shouldEqual ZonedDateTime.of(2018, 3, 6, 13, 0, 0, 0, ZoneId.systemDefault())
      actual(1).content shouldEqual "退会しちゃったんだよねぇ"
      actual(2).creator shouldEqual CybozuCSVUser("Takeshi Aaaaaaaaaaaa")
      actual(2).createdAt shouldEqual ZonedDateTime.of(2018, 4, 16, 2, 11, 0, 0, ZoneId.systemDefault())
      actual(2).content shouldEqual "ほーーーーーーーーい！"
      actual(3).creator shouldEqual CybozuCSVUser("Takahashi Takunomi")
      actual(3).createdAt shouldEqual ZonedDateTime.of(2018, 4, 17, 19, 22, 0, 0, ZoneId.systemDefault())
      actual(3).content shouldEqual "Shomaさん\r\n\r\nフィードバックいただいた件、対応完了しました。\r\n次のURLをご確認ください。\r\n--------------------------------------------------\r\n\r\nhttp://google.com"
    }
  }


  def source: String =
    """--------------------------------------------------
      |27: Shoma Nishitaten 2018/3/7 (火) 10:44
      |
      |ありがとうございます。
      |明日やってみます。
      |
      |--------------------------------------------------
      |26: (サービス退会者) 2018/3/6 (水) 13:00
      |
      |退会しちゃったんだよねぇ
      |
      |--------------------------------------------------
      |25: Takeshi Aaaaaaaaaaaa 2018/4/16 (火) 2:11
      |
      |ほーーーーーーーーい！
      |
      |--------------------------------------------------
      |24: Takahashi Takunomi 2018/4/17 (月) 19:22
      |
      |Shomaさん
      |
      |フィードバックいただいた件、対応完了しました。
      |次のURLをご確認ください。
      |--------------------------------------------------
      |
      |http://google.com
      |
      |--------------------------------------------------""".stripMargin.replace("\n", "\r\n")

}
