package com.nulabinc.backlog.c2b.core.domain.parser

import com.nulabinc.backlog.c2b.core.domain.model.CybozuComment
import org.apache.commons.csv.CSVRecord

sealed trait ParseError[A]
case class CannotParseCSV[A](klass: Class[A], record: CSVRecord) extends ParseError[A]
case class CannotParseFromString[A](klass: Class[A], value: String) extends ParseError[A] {
  override def toString: String = s"$klass $value"
}
case class CannotParseComment(reason: String, data: String) extends ParseError[CybozuComment]
