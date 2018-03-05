package com.nulabinc.backlog.c2b.parser.dsl

import com.nulabinc.backlog.c2b.core.domain.model._

object Algebra {

  sealed trait CsvParser[+A]

  case class ParseUser(content: String) extends CsvParser[Seq[CybozuUser]]
  case class ParseIssue(content: String) extends CsvParser[Seq[CybozuIssue]]

}
