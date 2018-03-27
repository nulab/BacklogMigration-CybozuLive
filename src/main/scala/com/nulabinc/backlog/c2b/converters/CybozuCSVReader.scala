package com.nulabinc.backlog.c2b.converters

import java.io.File
import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.parsers.CSVRecordParser
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.JavaConverters._

object CybozuCSVReader {

  val charset: Charset = Charset.forName("UTF-8")

  def toCybozuIssue(files: Array[File], csvFormat: CSVFormat): Observable[(CybozuCSVIssue, Seq[CybozuCSVComment])] =
    Observable
      .fromIterable(files)
      .flatMap { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.issue)
          .map {
            case Right(csvIssue) =>
              (csvIssue, csvIssue.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }
      }

//  def toComments(parentId: AnyId, comments: Seq[CybozuCSVComment]): Seq[CybozuComment] =
//    comments.map(c => CybozuComment.from(parentId, c))

  def toCybozuEvent(files: Array[File], csvFormat: CSVFormat): Observable[CybozuCSVEvent] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.event)
          .map {
            case Right(csvEvent) => csvEvent
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }

  def toCybozuForum(files: Array[File], csvFormat: CSVFormat): Observable[CybozuCSVForum] =
    Observable
      .fromIterable(files)
      .mapParallelUnordered(files.length) { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.forum)
          .map {
            case Right(csvForum) => csvForum
            case Left(error) => throw new RuntimeException(error.toString)
          }.headL
      }
}
