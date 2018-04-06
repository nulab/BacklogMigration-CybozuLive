package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.CybozuIssueType
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogIssueType

object BacklogIssueTypeConverter extends Converter[CybozuIssueType, BacklogIssueType] {
  override def to(a: CybozuIssueType): Either[ConvertError, BacklogIssueType] =
    Right(
      BacklogIssueType(
        optId = None,
        name = a.value,
        color = BacklogConstantValue.ISSUE_TYPE_COLOR.getStrValue,
        delete = false
      )
    )

}
