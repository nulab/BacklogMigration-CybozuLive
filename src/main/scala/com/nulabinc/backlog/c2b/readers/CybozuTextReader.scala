package com.nulabinc.backlog.c2b.readers

import java.io.File
import java.nio.charset.Charset

import monix.reactive.Observable

import scala.io.Source


case class TopicReadResult(topicText: String, comments: Stream[String])

object CybozuTopicTextReader {

  private val charset = Charset.forName("UTF-8")
  private val topicSeparator = "================================================================================"
  private val commentSeparator = "--------------------------------------------------------------------------------"

  private case class Last[T](topicText: String, comments: Stream[T], last: T)

  def read(files: Array[File]): Observable[TopicReadResult] =
    Observable
      .fromIterable(files)
      .flatMap { file =>
        Observable
          .fromIterator(Source.fromFile(file.getAbsolutePath, charset.name).getLines)
          .drop(1)
          .foldLeftF(Last("", Stream.empty[String], "")) {
            case (acc, line) =>
              if (line.startsWith(topicSeparator) || line.startsWith(commentSeparator)) {
                if (acc.topicText.isEmpty)
                  Last(acc.last, acc.comments, "")
                else
                  Last(acc.topicText, acc.comments :+ acc.last, "")
              } else {
                Last(acc.topicText, acc.comments, acc.last + line)
              }
          }.map { result =>
            TopicReadResult(result.topicText, result.comments)
          }
      }

}
