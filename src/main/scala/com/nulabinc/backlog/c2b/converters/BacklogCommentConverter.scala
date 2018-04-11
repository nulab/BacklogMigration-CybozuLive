package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas.{CybozuComment, MappingContext}
import com.nulabinc.backlog.migration.common.domain.BacklogComment
import com.nulabinc.backlog.migration.common.utils.StringUtil

class BacklogCommentConverter()(implicit ctx: MappingContext) {

  val userConverter = new BacklogUserConverter()

  def from(issueId: AnyId, from: CybozuComment): Either[ConvertError, BacklogComment] =
    for {
      convertedCreator <- userConverter.to(from.creator)
    } yield {
      BacklogComment(
        eventType = "comment",
        optIssueId = Some(issueId),
        optContent = Option(from.content).map(StringUtil.toSafeString),
        changeLogs = Seq(),
        notifications = Seq(),
        optCreatedUser = Some(convertedCreator),
        optCreated =  Some(DateUtil.toDateTimeString(from.createdAt))
      )
    }

}
