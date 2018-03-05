package com.nulabinc.backlog.c2b.parser.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.core.domain.model._

object CsvParser {

  import Algebra._

  type CsvParseProgram[A] = Free[CsvParser, A]

  def parseUser(content: String): CsvParseProgram[Seq[CybozuUser]] =
    Free.liftF(ParseUser(content))

  def parseIssue(content: String): CsvParseProgram[Seq[CybozuIssue]] =
    Free.liftF(ParseIssue(content))


}
