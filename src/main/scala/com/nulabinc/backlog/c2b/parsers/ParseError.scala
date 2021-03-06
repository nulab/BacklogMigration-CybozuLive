package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas.{CybozuCSVComment, CybozuTextPost}
import org.apache.commons.csv.CSVRecord

sealed trait ParseError[A]
case class CannotParseCSV[A](klass: Class[A], reason: String, record: CSVRecord) extends ParseError[A]
case class CannotParseFromString[A](klass: Class[A], reason: String, value: String) extends ParseError[A] {
  override def toString: String = s"$klass $reason Input: $value"
}
case class CannotParseComment(reason: String, data: String) extends ParseError[CybozuCSVComment]
case class CannotParsePost(reason: String, data: String) extends ParseError[CybozuTextPost]
