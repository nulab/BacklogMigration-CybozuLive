package com.nulabinc.backlog.c2b.parser

import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
import com.nulabinc.backlog.c2b.parser.dsl.CsvParseError
import zamblauskas.csv.parser.Parser

object CsvParser {

  import com.nulabinc.backlog.c2b.parser.dsl.ParseADT._
  import com.nulabinc.backlog.c2b.parser.formatters.ScalaCsvParserFormats._

  def parseUser(content: Content): Result[CybozuUser] =
    Parser.parse[CybozuUser](content) match {
      case Right(data) => Right(data)
      case Left(error) => Left(CsvParseError(error.lineNum, error.line, error.message))
    }

}
