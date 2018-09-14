package com.nulabinc.backlog.c2b.parsers

import org.scalatest.{FlatSpec, Matchers}

class TextFileParserSpec extends FlatSpec with Matchers {

  "TextFileParser.title" should "extract title from line" in {
    TextFileParser.title("Title: aaa") shouldEqual Right("aaa")
    TextFileParser.title("タイトル: テスト") shouldEqual Right("テスト")
  }

  "TextFileParser.user" should "extract username from line" in {
    TextFileParser.user("23: Shoma Nishitaten") shouldEqual Right("Shoma Nishitaten")
    TextFileParser.user("23: Shoma :Nishitaten") shouldEqual Right("Shoma :Nishitaten")
  }

  "TextFileParser.topic" should "parse multi-line description topic" in {
    val str =
      """
        |Title: multi
        |
        |aaa
        |bbb
        |ccc""".stripMargin

    val actual = TextFileParser.topic(str)
    actual.map(topic => topic.title) shouldEqual Right("multi")
    actual.map(topic => topic.description) shouldEqual Right("aaa\nbbb\nccc")
  }

  "TextFileParser.topic" should "parse single-line description topic" in {
    val str =
      """
        |Title: single
        |
        |aaa""".stripMargin

    val actual = TextFileParser.topic(str)
    actual.map(topic => topic.title) shouldEqual Right("single")
    actual.map(topic => topic.description) shouldEqual Right("aaa")
  }

  "TextFileParser.topic" should "parse no description topic" in {
    val str =
      """
        |Title: no description
        |
        |""".stripMargin

    val actual = TextFileParser.topic(str)
    actual.map(topic => topic.title) shouldEqual Right("no description")
    actual.map(topic => topic.description) shouldEqual Right("")
  }
}
