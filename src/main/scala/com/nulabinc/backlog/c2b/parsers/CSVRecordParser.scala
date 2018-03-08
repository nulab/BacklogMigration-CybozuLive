package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas._
import org.apache.commons.csv.CSVRecord

object CSVRecordParser {

  def user(record: CSVRecord): Either[ParseError[CybozuUser], CybozuUser] = {
    if (record.size() > 4) {
      Right(
        CybozuUser(
          lastName = record.get(0),
          firstName = record.get(1)
        )
      )
    } else {
      Left(CannotParseCSV(classOf[CybozuUser], "Invalid record size: " + record.size(), record))
    }
  }

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","ステータス","優先度","担当者","期日","コメント"
  def issue(record: CSVRecord): Either[ParseError[CybozuIssue], CybozuIssue] = {
    if (record.size() > 11) {
      (for {
        creator <- UserParser.toUser(record.get(3))
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(4))
        updater <- UserParser.toUser(record.get(5))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(6))
        maybeAssignee <- UserParser.toMaybeUser(record.get(9))
        dueDate <- ZonedDateTimeParser.toMaybeZonedDate(record.get(10))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(11)))
      } yield {
        CybozuIssue(
          id        = record.get(0),
          title     = record.get(1),
          content   = record.get(2),
          creator   = creator,
          createdAt = createdAt,
          updater   = updater,
          updatedAt = updatedAt,
          status    = CybozuStatus(record.get(7)),
          priority  = CybozuPriority(record.get(8)),
          assignee  = maybeAssignee,
          dueDate   = dueDate,
          comments  = comments
        )
      }) match {
        case Right(issue) => Right(issue)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuIssue], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuIssue], "Invalid record size: " + record.size(), record))
    }
  }

  // "開始日付","開始時刻","終了日付","終了時刻","予定メニュー","タイトル","メモ","作成者","コメント"
  def event(record: CSVRecord): Either[ParseError[CybozuEvent], CybozuEvent] = {

    val startDateFieldIndex  = 0
    val startTimeFieldIndex  = 1
    val endDateFieldIndex    = 2
    val endTimeFieldIndex    = 3
    val menuFieldIndex       = 4
    val titleFieldIndex      = 5
    val memoFieldIndex       = 6
    val creatorFieldIndex    = 7
    val commentFieldIndex    = 8

    if (record.size() >= CybozuEvent.fieldSize) {
      val startDate = record.get(startDateFieldIndex)
      val startTime = record.get(startTimeFieldIndex)
      val endDate = record.get(endDateFieldIndex)
      val endTime = record.get(endTimeFieldIndex)

      (for {
        startDateTime <- ZonedDateTimeParser.toZonedDateTime(startDate, startTime)
        endDateTime <- ZonedDateTimeParser.toZonedDateTime(endDate, endTime)
        creator <- UserParser.toUser(record.get(creatorFieldIndex))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(commentFieldIndex)))
      } yield {
        CybozuEvent(
          startDateTime = startDateTime,
          endDateTime = endDateTime,
          menu = ScheduledMenu(record.get(menuFieldIndex)),
          title = record.get(titleFieldIndex),
          memo = record.get(memoFieldIndex),
          creator = creator,
          comments = comments
        )
      }) match {
        case Right(event) => Right(event)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuEvent], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuEvent], "Invalid record size: " + record.size(), record))
    }
  }

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","コメント"
  def forum(record: CSVRecord): Either[ParseError[CybozuForum], CybozuForum] = {

    val idFieldIndex        = 0
    val titleFieldIndex     = 1
    val contentFieldIndex   = 2
    val creatorFieldIndex   = 3
    val createdAtFieldIndex = 4
    val updaterFieldIndex   = 5
    val updatedAtFieldIndex = 6
    val commentFieldIndex   = 7

    if (record.size() >= CybozuForum.fieldSize) {
      (for {
        creator <- UserParser.toUser(record.get(creatorFieldIndex))
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(createdAtFieldIndex))
        updater <- UserParser.toUser(record.get(updaterFieldIndex))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(updatedAtFieldIndex))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(commentFieldIndex)))
      } yield {
        CybozuForum(
          id = record.get(idFieldIndex),
          title = record.get(titleFieldIndex),
          content = record.get(contentFieldIndex),
          creator = creator,
          createdAt = createdAt,
          updater = updater,
          updatedAt = updatedAt,
          comments = comments
        )
      }) match {
        case Right(forum) => Right(forum)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuForum], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuForum], "Invalid record size: " + record.size(), record))
    }
  }

}
