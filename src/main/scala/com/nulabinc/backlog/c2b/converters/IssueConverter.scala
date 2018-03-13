package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas.CybozuIssue
import com.nulabinc.backlog.migration.common.domain._

object IssueConverter {

  def toBacklogIssue(issue: CybozuIssue)(implicit ctx: MappingContext): Either[ConvertError[CybozuIssue], BacklogIssue] = {

    /*
    cybozuUser: CybozuUser <- userId: String

   */

    for {
      createdUser <- UserConverter.toBacklogUser(issue.creatorId)
      updaterUser <- UserConverter.toBacklogUser(issue.updaterId)
      maybeAssignee <- issue.assigneeId.map(UserConverter.toBacklogUser)
      status <- ctx.getStatusName(issue.status)
      priority <- ctx.getPriorityName(issue.priority)
    } yield {
      Right(
        BacklogIssue(
          eventType = "issue",
          id = issue.id,
          optIssueKey = None,
          summary = BacklogIssueSummary(value = issue.title, original = issue.title),
          optParentIssueId = None,
          description = issue.content,
          optStartDate = None,
          optDueDate = issue.dueDate.map(DateUtil.toDateString),
          optEstimatedHours = None,
          optActualHours = None,
          optIssueTypeName = None,
          statusName = status,
          categoryNames = Seq.empty[String],
          versionNames = Seq.empty[String],
          milestoneNames = Seq.empty[String],
          priorityName = priority,
          optAssignee = maybeAssignee,
          attachments = Seq.empty[BacklogAttachment],
          sharedFiles = Seq.empty[BacklogSharedFile],
          customFields = Seq.empty[BacklogCustomField],
          notifiedUsers = Seq.empty[BacklogUser],
          operation = BacklogOperation(
            optCreatedUser = Some(createdUser),
            optCreated = Some(DateUtil.toDateTimeString(issue.createdAt)),
            optUpdatedUser = Some(updaterUser),
            optUpdated = Some(DateUtil.toDateTimeString(issue.updatedAt))
          )
        )
      )
    }

  }
}
