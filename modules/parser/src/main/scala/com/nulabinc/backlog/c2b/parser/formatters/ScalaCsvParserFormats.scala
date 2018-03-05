package com.nulabinc.backlog.c2b.parser.formatters

import zamblauskas.csv.parser._
import zamblauskas.functional._
import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser

object ScalaCsvParserFormats {

  implicit val userReads: ColumnReads[CybozuUser] = (
    column("姓").as[String] and
    column("名").as[String] and
    column("メールアドレス").as[String]
  )(CybozuUser)

}
