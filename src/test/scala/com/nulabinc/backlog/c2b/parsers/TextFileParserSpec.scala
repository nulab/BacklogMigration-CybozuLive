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
}
