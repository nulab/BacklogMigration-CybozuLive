package com.nulabinc.backlog.c2b.parser

import org.apache.commons.csv.CSVRecord

sealed trait CSVParseError[A]
case class CannotParseCSV[A](klass: Class[A], record: CSVRecord) extends CSVParseError[A]
