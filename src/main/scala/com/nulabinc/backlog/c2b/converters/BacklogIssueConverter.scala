package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.migration.common.domain._


sealed trait IssueFrom

case class FromCybozuEvent(
  event: CybozuDBEvent,
  creator: CybozuDBUser
) extends IssueFrom

case class FromCybozuForum(
  forum: CybozuDBForum,
  creator: CybozuDBUser,
  updater: CybozuDBUser
) extends IssueFrom


class IssueConverter()(implicit ctx: MappingContext) {

  import com.nulabinc.backlog.c2b.syntax.EitherOps._

  val userConverter = new BacklogUserConverter()

  def from(from: CybozuTodo): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "ToDoリスト"

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
          from.todo.title + "\n\n担当者: " + otherAssignees.mkString(",")
        } else {
          from.todo.title
        }
        defaultBacklogIssue.copy(
          id                = from.todo.id,
          summary           = BacklogIssueSummary(value = title, original = title),
          description       = from.todo.content,
          optDueDate        = from.todo.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(ISSUE_TYPE_NAME),
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
  }

  def from(fromCybozuEvent: FromCybozuEvent): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "イベント"

    for {
      convertedCreator <- userConverter.to(fromCybozuEvent.creator)
    } yield {
      defaultBacklogIssue.copy(
        id                = fromCybozuEvent.event.id,
        summary           = createBacklogIssueSummary(fromCybozuEvent.event.title),
        description       = fromCybozuEvent.event.memo + "\n\n" + fromCybozuEvent.event.menu,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(ISSUE_TYPE_NAME),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(fromCybozuEvent.event.startDateTime)),
          optUpdatedUser    = None,
          optUpdated        = None
        )
      )
    }
  }

  def from(fromCybozuForum: FromCybozuForum): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "掲示板"

    for {
      convertedCreator <- userConverter.to(fromCybozuForum.creator)
      convertedUpdater <- userConverter.to(fromCybozuForum.updater)
    } yield {
      defaultBacklogIssue.copy(
        id                = fromCybozuForum.forum.id,
        summary           = BacklogIssueSummary(value = fromCybozuForum.forum.title, original = fromCybozuForum.forum.title),
        description       = fromCybozuForum.forum.content,
        optStartDate      = None,
        optDueDate        = None,
        optIssueTypeName  = Some(ISSUE_TYPE_NAME),
        operation         = BacklogOperation(
          optCreatedUser    = Some(convertedCreator),
          optCreated        = Some(DateUtil.toDateTimeString(fromCybozuForum.forum.createdAt)),
          optUpdatedUser    = Some(convertedUpdater),
          optUpdated        = Some(DateUtil.toDateTimeString(fromCybozuForum.forum.updatedAt))
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
