package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogIssueType

object BacklogIssueTypeConverter extends Converter[String, BacklogIssueType] {
  override def to(a: String): Either[ConvertError, BacklogIssueType] =
    Right(
      BacklogIssueType(
        optId = None,
        name = a,
        color = BacklogConstantValue.ISSUE_TYPE_COLOR.getStrValue,
        delete = false
      )
    )

}
