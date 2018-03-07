package com.nulabinc.backlog.c2b.core.domain.parser

import com.nulabinc.backlog.c2b.core.domain.model.{CybozuIssue, CybozuUser}
import org.apache.commons.csv.CSVRecord

object CSVRecordParser {

  def user(record: CSVRecord): Either[CSVParseError[CybozuUser], CybozuUser] = {
    if (record.size() > 4) {
      Right(
        CybozuUser(
          lastName = record.get(0),
          firstName = record.get(1),
          emailAddress = record.get(4)
        )
      )
    } else {
      Left(CannotParseCSV(classOf[CybozuUser], record))
    }
  }

  // "ID","タイトル","本文","作成者","作成日時","更新者","更新日時","ステータス","優先度","担当者","期日","コメント"
  def issue(line: String): CybozuIssue = ???

}
