package com.nulabinc.backlog.c2b.converters

import java.io.File
import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.parsers.CSVRecordParser
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.JavaConverters._

object CybozuIssueConverter {

  def to(files: Array[File], csvFormat: CSVFormat): Observable[CybozuIssue] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.issue)
          .map {
            case Right(csvIssue) => CybozuIssue.from(csvIssue)
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }
}
