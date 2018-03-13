package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas.CybozuIssue
import com.nulabinc.backlog.migration.common.domain._

object IssueConverter {

  def toBacklogIssue(issue: CybozuIssue): BacklogIssue =

  /*
    cybozuUser: CybozuUser <- userId: String

   */

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
      statusName = issue.status, // TODO: mapping
      categoryNames = Seq.empty[String],
      versionNames = Seq.empty[String],
      milestoneNames = Seq.empty[String],
      priorityName = issue.priority,  // TODO: mapping
      optAssignee = None,                           // TODO: convert to backlog user
      attachments = Seq.empty[BacklogAttachment],
      sharedFiles = Seq.empty[BacklogSharedFile],
      customFields = Seq.empty[BacklogCustomField],
      notifiedUsers = Seq.empty[BacklogUser],
      operation = BacklogOperation(
        optCreatedUser  = None, // TODO
        optCreated      = Some(DateUtil.toDateTimeString(issue.createdAt)),
        optUpdatedUser  = None, // TODO
        optUpdated      = Some(DateUtil.toDateTimeString(issue.updatedAt))
      )
    )

}
