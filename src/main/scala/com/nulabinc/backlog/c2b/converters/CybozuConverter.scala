package com.nulabinc.backlog.c2b.converters

import java.io.File
import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.parsers.CSVRecordParser
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.JavaConverters._

object CybozuConverter {

  def toIssue(files: Array[File], csvFormat: CSVFormat): Observable[(CybozuIssue, Seq[CybozuCSVComment])] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.issue)
          .map {
            case Right(csvIssue) => (CybozuIssue.from(csvIssue), csvIssue.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }

  def toComments(parentIssueId: AnyId, comments: Seq[CybozuCSVComment]): Seq[CybozuComment] =
    comments.map(c => CybozuComment.from(parentIssueId, c))

  def toEvent(files: Array[File], csvFormat: CSVFormat): Observable[(CybozuEvent, Seq[CybozuCSVComment])] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.event)
          .map {
            case Right(csvEvent) => (CybozuEvent.from(csvEvent), csvEvent.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }

  def toForum(files: Array[File], csvFormat: CSVFormat): Observable[(CybozuForum, Seq[CybozuCSVComment])] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.forum)
          .map {
            case Right(csvForum) => (CybozuForum.from(csvForum), csvForum.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }
}
