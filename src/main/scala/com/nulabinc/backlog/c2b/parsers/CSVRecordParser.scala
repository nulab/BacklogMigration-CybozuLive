package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas._
import org.apache.commons.csv.CSVRecord

object CSVRecordParser {

  def user(record: CSVRecord): Either[ParseError[CybozuUser], CybozuUser] = {

    val LAST_NAME_FIELD_INDEX   = 0
    val FIRST_NAME_FIELD_INDEX  = 1

    if (record.size() >= CybozuUser.fieldSize) {
      Right(
        CybozuUser(
          lastName = record.get(LAST_NAME_FIELD_INDEX),
          firstName = record.get(FIRST_NAME_FIELD_INDEX)
        )
      )
    } else {
      Left(CannotParseCSV(classOf[CybozuUser], "Invalid record size: " + record.size(), record))
    }
  }

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","ステータス","優先度","担当者","期日","コメント"
  def issue(record: CSVRecord): Either[ParseError[CybozuIssue], CybozuIssue] = {

    val ID_FIELD_INDEX          = 0
    val TITLE_FIELD_INDEX       = 1
    val CONTENT_FIELD_INDEX     = 2
    val CREATOR_FIELD_INDEX     = 3
    val CREATED_AT_FIELD_INDEX  = 4
    val UPDATER_FIELD_INDEX     = 5
    val UPDATED_AT_FIELD_INDEX  = 6
    val STATUS_FIELD_INDEX      = 7
    val PRIORITY_FIELD_INDEX    = 8
    val ASSIGNEE_FIELD_INDEX    = 9
    val DUE_DATE_FIELD_INDEX    = 10
    val COMMENTS_FIELD_INDEX    = 11

    if (record.size() >= CybozuIssue.fieldSize) {
      (for {
        creator <- UserParser.toUser(record.get(CREATOR_FIELD_INDEX))
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(CREATED_AT_FIELD_INDEX))
        updater <- UserParser.toUser(record.get(UPDATER_FIELD_INDEX))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(UPDATED_AT_FIELD_INDEX))
        maybeAssignee <- UserParser.toMaybeUser(record.get(ASSIGNEE_FIELD_INDEX))
        dueDate <- ZonedDateTimeParser.toMaybeZonedDate(record.get(DUE_DATE_FIELD_INDEX))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENTS_FIELD_INDEX)))
      } yield {
        CybozuIssue(
          id        = record.get(ID_FIELD_INDEX),
          title     = record.get(TITLE_FIELD_INDEX),
          content   = record.get(CONTENT_FIELD_INDEX),
          creator   = creator,
          createdAt = createdAt,
          updater   = updater,
          updatedAt = updatedAt,
          status    = CybozuStatus(record.get(STATUS_FIELD_INDEX)),
          priority  = CybozuPriority(record.get(PRIORITY_FIELD_INDEX)),
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

    val START_DATE_FIELD_INDEX  = 0
    val START_TIME_FIELD_INDEX  = 1
    val END_DATE_FIELD_INDEX    = 2
    val END_TIME_FIELD_INDEX    = 3
    val MENU_FIELD_INDEX        = 4
    val TITLE_FIELD_INDEX       = 5
    val MEMO_FIELD_INDEX        = 6
    val CREATOR_FIELD_INDEX     = 7
    val COMMENT_FIELD_INDEX     = 8

    if (record.size() >= CybozuEvent.fieldSize) {
      val startDate = record.get(START_DATE_FIELD_INDEX)
      val startTime = record.get(START_TIME_FIELD_INDEX)
      val endDate = record.get(END_DATE_FIELD_INDEX)
      val endTime = record.get(END_TIME_FIELD_INDEX)

      (for {
        startDateTime <- ZonedDateTimeParser.toZonedDateTime(startDate, startTime)
        endDateTime <- ZonedDateTimeParser.toZonedDateTime(endDate, endTime)
        creator <- UserParser.toUser(record.get(CREATOR_FIELD_INDEX))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENT_FIELD_INDEX)))
      } yield {
        CybozuEvent(
          startDateTime = startDateTime,
          endDateTime = endDateTime,
          menu = ScheduledMenu(record.get(MENU_FIELD_INDEX)),
          title = record.get(TITLE_FIELD_INDEX),
          memo = record.get(MEMO_FIELD_INDEX),
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

    val ID_FIELD_INDEX          = 0
    val TITLE_FIELD_INDEX       = 1
    val CONTENT_FIELD_INDEX     = 2
    val CREATOR_FIELD_INDEX     = 3
    val CREATED_AT_FIELD_INDEX  = 4
    val UPDATER_FIELD_INDEX     = 5
    val UPDATED_AT_FIELD_INDEX  = 6
    val COMMENT_FIELD_INDEX     = 7

    if (record.size() >= CybozuForum.fieldSize) {
      (for {
        creator <- UserParser.toUser(record.get(CREATOR_FIELD_INDEX))
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(CREATED_AT_FIELD_INDEX))
        updater <- UserParser.toUser(record.get(UPDATER_FIELD_INDEX))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(UPDATED_AT_FIELD_INDEX))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENT_FIELD_INDEX)))
      } yield {
        CybozuForum(
          id = record.get(ID_FIELD_INDEX),
          title = record.get(TITLE_FIELD_INDEX),
          content = record.get(CONTENT_FIELD_INDEX),
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
