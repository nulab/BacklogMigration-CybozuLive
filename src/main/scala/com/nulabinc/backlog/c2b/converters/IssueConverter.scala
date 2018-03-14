package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas.{CybozuEvent, CybozuForum, CybozuIssue, CybozuUser}
import com.nulabinc.backlog.migration.common.domain._


case class BacklogIssueParams(issue: CybozuIssue,
                              creator: CybozuUser,
                              updater: CybozuUser,
                              maybeAssignee: Option[CybozuUser])


class IssueConverter()(implicit ctx: MappingContext) extends Converter[BacklogIssueParams, BacklogIssue]{

  val userConverter = new UserConverter()

  def to(params: BacklogIssueParams): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "ToDoリスト"

    for {
      convertedCreator <- userConverter.to(params.creator)
      convertedUpdater <- userConverter.to(params.updater)
      maybeConvertedAssignee <- userConverter.to(params.maybeAssignee)
      t = maybeConvertedAssignee
      status <- ctx.getStatusName(params.issue.status)
      priority <- ctx.getPriorityName(params.issue.priority)
    } yield {
        defaultBacklogIssue.copy(
          id                = params.issue.id,
          summary           = BacklogIssueSummary(value = params.issue.title, original = params.issue.title),
          description       = params.issue.content,
          optDueDate        = params.issue.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(ISSUE_TYPE_NAME),
          statusName        = status,
          priorityName      = priority,
          optAssignee       = maybeConvertedAssignee,
          operation         = BacklogOperation(
            optCreatedUser    = Some(convertedCreator),
            optCreated        = Some(DateUtil.toDateTimeString(params.issue.createdAt)),
            optUpdatedUser    = Some(convertedUpdater),
            optUpdated        = Some(DateUtil.toDateTimeString(params.issue.updatedAt))
          )
        )
    }
  }

  def toBacklogIssue(event: CybozuEvent,
                     creator: CybozuUser): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "イベント"

    for {
      convertedCreator <- userConverter.to(creator)
    } yield {
      defaultBacklogIssue.copy(
        id                = event.id,
        summary           = BacklogIssueSummary(value = event.title, original = event.title),
        description       = event.memo + "\n\n" + event.menu,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(ISSUE_TYPE_NAME),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(event.startDateTime)),
          optUpdatedUser    = None,
          optUpdated        = None
        )
      )
    }
  }

  def toBacklogIssue(forum: CybozuForum,
                     creator: CybozuUser,
                     updater: CybozuUser): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "掲示板"

    for {
      convertedCreator <- userConverter.to(creator)
      convertedUpdater <- userConverter.to(updater)
    } yield {
      defaultBacklogIssue.copy(
        id                = forum.id,
        summary           = BacklogIssueSummary(value = forum.title, original = forum.title),
        description       = forum.content,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(ISSUE_TYPE_NAME),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(forum.createdAt)),
          optUpdatedUser    = Some(convertedUpdater),
          optUpdated        = Some(DateUtil.toDateTimeString(forum.updatedAt))
        )
      )
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
