package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.datas._
import org.apache.commons.csv.CSVRecord

object CSVRecordParser {

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","ステータス","優先度","担当者","期日","コメント"
  def issue(record: CSVRecord): Either[ParseError[CybozuCSVTodo], CybozuCSVTodo] = {

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

    val assignee = record.get(ASSIGNEE_FIELD_INDEX)

    if (record.size() >= CybozuCSVTodo.fieldSize) {
      (for {
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(CREATED_AT_FIELD_INDEX))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(UPDATED_AT_FIELD_INDEX))
        dueDate <- ZonedDateTimeParser.toMaybeZonedDateTime(record.get(DUE_DATE_FIELD_INDEX))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENTS_FIELD_INDEX)))
      } yield {
        CybozuCSVTodo(
          id        = record.get(ID_FIELD_INDEX),
          title     = record.get(TITLE_FIELD_INDEX),
          content   = record.get(CONTENT_FIELD_INDEX),
          creator   = CybozuCSVUser(record.get(CREATOR_FIELD_INDEX)),
          createdAt = createdAt,
          updater   = CybozuCSVUser(record.get(UPDATER_FIELD_INDEX)),
          updatedAt = updatedAt,
          status    = CybozuCSVStatus(record.get(STATUS_FIELD_INDEX)),
          priority  = CybozuCSVPriority(record.get(PRIORITY_FIELD_INDEX)),
          assignees  = assignee.split(",").filter(_.nonEmpty).map(u => CybozuCSVUser(u)),
          dueDate   = dueDate,
          comments  = filterEmptyComment(comments)
        )
      }) match {
        case Right(issue) => Right(issue)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuCSVTodo], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuCSVTodo], "Invalid record size: " + record.size(), record))
    }
  }

  // "開始日付","開始時刻","終了日付","終了時刻","予定メニュー","タイトル","メモ","作成者","コメント"
  def event(record: CSVRecord): Either[ParseError[CybozuCSVEvent], CybozuCSVEvent] = {

    val START_DATE_FIELD_INDEX  = 0
    val START_TIME_FIELD_INDEX  = 1
    val END_DATE_FIELD_INDEX    = 2
    val END_TIME_FIELD_INDEX    = 3
    val MENU_FIELD_INDEX        = 4
    val TITLE_FIELD_INDEX       = 5
    val MEMO_FIELD_INDEX        = 6
    val CREATOR_FIELD_INDEX     = 7
    val COMMENT_FIELD_INDEX     = 8

    if (record.size() >= CybozuCSVEvent.fieldSize) {
      val startDate = record.get(START_DATE_FIELD_INDEX)
      val startTime = record.get(START_TIME_FIELD_INDEX)
      val endDate = record.get(END_DATE_FIELD_INDEX)
      val endTime = record.get(END_TIME_FIELD_INDEX)

      (for {
        startDateTime <- ZonedDateTimeParser.toZonedDateTime(startDate, startTime)
        endDateTime <- ZonedDateTimeParser.toZonedDateTime(endDate, endTime)
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENT_FIELD_INDEX)))
      } yield {
        CybozuCSVEvent(
          startDateTime = startDateTime,
          endDateTime = endDateTime,
          menu = record.get(MENU_FIELD_INDEX),
          title = record.get(TITLE_FIELD_INDEX),
          memo = record.get(MEMO_FIELD_INDEX),
          creator = CybozuCSVUser(record.get(CREATOR_FIELD_INDEX)),
          comments = filterEmptyComment(comments)
        )
      }) match {
        case Right(event) => Right(event)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuCSVEvent], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuCSVEvent], "Invalid record size: " + record.size(), record))
    }
  }

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","コメント"
  def forum(record: CSVRecord): Either[ParseError[CybozuCSVForum], CybozuCSVForum] = {

    val ID_FIELD_INDEX          = 0
    val TITLE_FIELD_INDEX       = 1
    val CONTENT_FIELD_INDEX     = 2
    val CREATOR_FIELD_INDEX     = 3
    val CREATED_AT_FIELD_INDEX  = 4
    val UPDATER_FIELD_INDEX     = 5
    val UPDATED_AT_FIELD_INDEX  = 6
    val COMMENT_FIELD_INDEX     = 7

    if (record.size() >= CybozuCSVForum.fieldSize) {
      (for {
        createdAt <- ZonedDateTimeParser.toZonedDateTime(record.get(CREATED_AT_FIELD_INDEX))
        updatedAt <- ZonedDateTimeParser.toZonedDateTime(record.get(UPDATED_AT_FIELD_INDEX))
        comments <- CommentParser.sequence(CommentParser.parse(record.get(COMMENT_FIELD_INDEX)))
      } yield {
        CybozuCSVForum(
          id = record.get(ID_FIELD_INDEX),
          title = record.get(TITLE_FIELD_INDEX),
          content = record.get(CONTENT_FIELD_INDEX),
          creator = CybozuCSVUser(record.get(CREATOR_FIELD_INDEX)),
          createdAt = createdAt,
          updater = CybozuCSVUser(record.get(UPDATER_FIELD_INDEX)),
          updatedAt = updatedAt,
          comments = filterEmptyComment(comments)
        )
      }) match {
        case Right(forum) => Right(forum)
        case Left(error) => Left(CannotParseCSV(classOf[CybozuCSVForum], error.toString, record))
      }
    } else {
      Left(CannotParseCSV(classOf[CybozuCSVForum], "Invalid record size: " + record.size(), record))
    }
  }

  private def filterEmptyComment(comments: Seq[CybozuCSVComment]): Seq[CybozuCSVComment] =
    comments.filter(_.content.nonEmpty)

}
