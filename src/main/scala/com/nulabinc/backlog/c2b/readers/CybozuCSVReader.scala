package com.nulabinc.backlog.c2b.readers

import java.io.File
import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.parsers.CSVRecordParser
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.JavaConverters._

case class ReadResult[A](data: A, comments: Seq[CybozuCSVComment])

object CybozuCSVReader {

  val charset: Charset = Charset.forName("UTF-8")

  def toCybozuIssue(files: Array[File], csvFormat: CSVFormat): Observable[ReadResult[CybozuCSVIssue]] =
    Observable
      .fromIterable(files)
      .flatMap { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.issue)
          .map {
            case Right(csvIssue) => ReadResult(csvIssue, csvIssue.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }
      }

  def toCybozuEvent(files: Array[File], csvFormat: CSVFormat): Observable[ReadResult[CybozuCSVEvent]] =
    Observable
      .fromIterable(files)
      .flatMap { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.event)
          .map {
            case Right(csvEvent) => ReadResult(csvEvent, csvEvent.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }
      }

  def toCybozuForum(files: Array[File], csvFormat: CSVFormat): Observable[ReadResult[CybozuCSVForum]] =
    Observable
      .fromIterable(files)
      .flatMap { file =>
        Observable.fromIterator(CSVParser.parse(file, charset, csvFormat).iterator().asScala)
          .drop(1)
          .map(CSVRecordParser.forum)
          .map {
            case Right(csvForum) => ReadResult(csvForum, csvForum.comments)
            case Left(error) => throw new RuntimeException(error.toString)
          }
      }
}
