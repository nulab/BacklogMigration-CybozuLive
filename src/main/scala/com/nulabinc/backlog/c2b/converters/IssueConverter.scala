package com.nulabinc.backlog.c2b.converters

import java.time.format.DateTimeFormatter

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.migration.common.domain._

class IssueConverter()(implicit ctx: MappingContext) {

  import com.nulabinc.backlog.c2b.syntax.EitherOps._

  private val userConverter = new BacklogUserConverter()
  private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

  def from(from: CybozuTodo, issueType: CybozuIssueType): Either[ConvertError, BacklogIssue] =
    for {
      convertedCreator <- userConverter.to(from.creator)
      convertedUpdater <- userConverter.to(from.updater)
      assignees <- from.assignees.map(u => userConverter.to(u)).sequence
      status <- ctx.getStatusName(from.todo.status)
      priority <- ctx.getPriorityName(from.todo.priority)
    } yield {
        val optAssignee = if (assignees.length > 1) Some(assignees.head) else None
        val title = if (assignees.length > 1) {
          val otherAssignees = assignees.tail
          from.todo.title + "\n\n担当者: " + otherAssignees.map(_.optUserId.getOrElse("")).mkString(",")
        } else {
          from.todo.title
        }
        defaultBacklogIssue.copy(
          id                = from.todo.id,
          summary           = createBacklogIssueSummary(title),
          description       = from.todo.content,
          optDueDate        = from.todo.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(issueType.value),
          statusName        = status,
          priorityName      = priority,
          optAssignee       = optAssignee,
          operation         = BacklogOperation(
            optCreatedUser    = Some(convertedCreator),
            optCreated        = Some(DateUtil.toDateTimeString(from.todo.createdAt)),
            optUpdatedUser    = Some(convertedUpdater),
            optUpdated        = Some(DateUtil.toDateTimeString(from.todo.updatedAt))
          )
        )
    }

  def from(from: CybozuEvent, issueType: CybozuIssueType): Either[ConvertError, BacklogIssue] =
    for {
      convertedCreator <- userConverter.to(from.creator)
    } yield {
      val description =
        from.event.memo + "\n\n" +
        from.event.menu + "\n\n" +
        formatter.format(from.event.startDateTime) + " ~ " + formatter.format(from.event.endDateTime)
      defaultBacklogIssue.copy(
        id                = from.event.id,
        summary           = createBacklogIssueSummary(from.event.title),
        description       = description,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(issueType.value),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(from.event.startDateTime)),
          optUpdatedUser    = None,
          optUpdated        = None
        )
      )
    }

  def from(from: CybozuForum, issueType: CybozuIssueType): Either[ConvertError, BacklogIssue] = {

    for {
      convertedCreator <- userConverter.to(from.creator)
      convertedUpdater <- userConverter.to(from.updater)
    } yield {
      defaultBacklogIssue.copy(
        id                = from.forum.id,
        summary           = createBacklogIssueSummary(from.forum.title),
        description       = from.forum.content,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(issueType.value),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(from.forum.createdAt)),
          optUpdatedUser    = Some(convertedUpdater),
          optUpdated        = Some(DateUtil.toDateTimeString(from.forum.updatedAt))
        )
      )
    }
  }

  private def createBacklogIssueSummary(summary: String): BacklogIssueSummary =
    BacklogIssueSummary(value = summary, original = summary)

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
      notifiedUsers = Seq.empty[com.nulabinc.backlog.migration.common.domain.BacklogUser],
      operation = BacklogOperation(
        optCreatedUser = None,
        optCreated = None,
        optUpdatedUser = None,
        optUpdated = None
      )
    )
}
