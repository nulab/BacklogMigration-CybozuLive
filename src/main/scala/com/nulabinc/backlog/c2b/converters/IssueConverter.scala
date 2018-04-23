package com.nulabinc.backlog.c2b.converters

import java.time.format.DateTimeFormatter

import com.nulabinc.backlog.c2b.core.{DateUtil, Logger}
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.migration.common.domain._
import com.osinka.i18n.Messages

class IssueConverter()(implicit ctx: MappingContext) extends Logger {

  import com.nulabinc.backlog.c2b.syntax.EitherOps._

  private val userConverter = new UserConverter()
  private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

  def from(from: CybozuTodo, issueType: CybozuIssueType): Either[ConvertError, BacklogIssue] =
    for {
      convertedCreator <- userConverter.to(from.creator)
      convertedUpdater <- userConverter.to(from.updater)
      assignees <- from.assignees.map(u => userConverter.to(u)).sequence
      status <- ctx.getStatusName(from.status)
      priority <- ctx.getPriorityName(from.priority)
    } yield {
        val description = if (assignees.length > 1) {
          val otherAssignees = assignees.tail
          from.content + s"\n\n${Messages("convert.other_assignees")}: " + otherAssignees.map(_.optUserId.getOrElse("")).mkString(",")
        } else {
          from.content
        } + from.dueDate.map(formatter.format).map(str => s"\n\n${Messages("convert.due_date")}: $str").getOrElse("")
        defaultBacklogIssue.copy(
          id                = from.id,
          summary           = createBacklogIssueSummary(from.title),
          description       = description,
          optDueDate        = from.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(issueType.value),
          statusName        = status,
          priorityName      = priority,
          optAssignee       = assignees.headOption,
          operation         = BacklogOperation(
            optCreatedUser    = Some(convertedCreator),
            optCreated        = Some(DateUtil.toDateTimeString(from.createdAt)),
            optUpdatedUser    = Some(convertedUpdater),
            optUpdated        = Some(DateUtil.toDateTimeString(from.updatedAt))
          )
        )
    }

  def from(from: CybozuEvent, issueType: CybozuIssueType): Either[ConvertError, BacklogIssue] =
    for {
      convertedCreator <- userConverter.to(from.creator)
    } yield {
      val description =
        from.memo + "\n\n" +
        from.menu + "\n\n" +
        formatter.format(from.startDateTime) + " ~ " + formatter.format(from.endDateTime)
      defaultBacklogIssue.copy(
        id                = from.id,
        summary           = createBacklogIssueSummary(from.title),
        description       = description,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(issueType.value),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(from.startDateTime)),
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
        id                = from.id,
        summary           = createBacklogIssueSummary(from.title),
        description       = from.content,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(issueType.value),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(from.createdAt)),
          optUpdatedUser    = Some(convertedUpdater),
          optUpdated        = Some(DateUtil.toDateTimeString(from.updatedAt))
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
