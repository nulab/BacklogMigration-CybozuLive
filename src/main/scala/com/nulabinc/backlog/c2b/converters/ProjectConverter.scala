package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.migration.common.domain.BacklogProject
import com.nulabinc.backlog4j.Project.TextFormattingRule

object ProjectConverter extends Converter[String, BacklogProject] {
  override def to(a: String): Either[ConvertError, BacklogProject] =
    Right(
      BacklogProject(
        optId = None,
        name = a,
        key = a,
        isChartEnabled = true,
        isSubtaskingEnabled = true,
        textFormattingRule = TextFormattingRule.Markdown.getStrValue
      )
    )
}
