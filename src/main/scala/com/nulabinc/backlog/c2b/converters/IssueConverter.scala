package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas.{CybozuIssue, CybozuUser}
import com.nulabinc.backlog.migration.common.domain._

object IssueConverter {

  def toBacklogIssue(issue: CybozuIssue,
                     createdUser: CybozuUser,
                     updaterUser: CybozuUser,
                     maybeAssignee: Option[CybozuUser])
                    (implicit ctx: MappingContext): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "ToDoリスト"

    for {
      convertedCreatedUser <- UserConverter.toBacklogUser(createdUser)
      convertedUpdaterUser <- UserConverter.toBacklogUser(updaterUser)
      maybeConvertedAssignee <- UserConverter.toBacklogUser(maybeAssignee)
      status <- ctx.getStatusName(issue.status)
      priority <- ctx.getPriorityName(issue.priority)
    } yield {
//      Right(
        defaultBacklogIssue.copy(
          id                = issue.id,
          summary           = BacklogIssueSummary(value = issue.title, original = issue.title),
          description       = issue.content,
          optDueDate        = issue.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(ISSUE_TYPE_NAME),
          statusName        = status,
          priorityName      = priority,
          optAssignee       = maybeConvertedAssignee,
          operation         = BacklogOperation(
            optCreatedUser    = Some(convertedCreatedUser),
            optCreated        = Some(DateUtil.toDateTimeString(issue.createdAt)),
            optUpdatedUser    = Some(convertedUpdaterUser),
            optUpdated        = Some(DateUtil.toDateTimeString(issue.updatedAt))
          )
        )
//      )
    }
  }

  private val defaultBacklogIssue: BacklogIssue =
    BacklogIssue(
      eventType = "issue",
      id = 0,
      optIssueKey = None,
      summary = BacklogIssueSummary(value = "", original = ""),
      optParentIssueId = None,
      description = "",
      optStartDate = None,
      optDueDate = None,
      optEstimatedHours = None,
      optActualHours = None,
      optIssueTypeName = None,
      statusName = "",
      categoryNames = Seq.empty[String],
      versionNames = Seq.empty[String],
      milestoneNames = Seq.empty[String],
      priorityName = "",
      optAssignee = None,
      attachments = Seq.empty[BacklogAttachment],
      sharedFiles = Seq.empty[BacklogSharedFile],
      customFields = Seq.empty[BacklogCustomField],
      notifiedUsers = Seq.empty[BacklogUser],
      operation = BacklogOperation(
        optCreatedUser = None,
        optCreated = None,
        optUpdatedUser = None,
        optUpdated = None
      )
    )
}
