package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.core.DateUtil
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.migration.common.domain._


sealed trait IssueFrom

case class FromCybozuTodo(
  todo: CybozuTodo,
  creator: CybozuUser,
  updater: CybozuUser,
  maybeAssignee: Option[CybozuUser]
) extends IssueFrom

case class FromCybozuEvent(
  event: CybozuEvent,
  creator: CybozuUser
) extends IssueFrom

case class FromCybozuForum(
  forum: CybozuForum,
  creator: CybozuUser,
  updater: CybozuUser
) extends IssueFrom


class IssueConverter()(implicit ctx: MappingContext) extends Converter[IssueFrom, BacklogIssue]{

  val userConverter = new BacklogUserConverter()

  override def to(issueFrom: IssueFrom): Either[ConvertError, BacklogIssue] = issueFrom match {
    case fromCybozuTodo: FromCybozuTodo =>
      from(fromCybozuTodo)
    case fromCybozuEvent: FromCybozuEvent =>
      from(fromCybozuEvent)
    case fromCybozuForum: FromCybozuForum =>
      from(fromCybozuForum)
  }

  def from(fromCybozuIssue: FromCybozuTodo): Either[ConvertError, BacklogIssue] = {

    val ISSUE_TYPE_NAME = "ToDoリスト"

    for {
      convertedCreator <- userConverter.to(fromCybozuIssue.creator)
      convertedUpdater <- userConverter.to(fromCybozuIssue.updater)
      maybeConvertedAssignee <- userConverter.to(fromCybozuIssue.maybeAssignee)
      status <- ctx.getStatusName(fromCybozuIssue.todo.status)
      priority <- ctx.getPriorityName(fromCybozuIssue.todo.priority)
    } yield {
        defaultBacklogIssue.copy(
          id                = fromCybozuIssue.todo.id,
          summary           = BacklogIssueSummary(value = fromCybozuIssue.todo.title, original = fromCybozuIssue.todo.title),
          description       = fromCybozuIssue.todo.content,
          optDueDate        = fromCybozuIssue.todo.dueDate.map(DateUtil.toDateString),
          optIssueTypeName  = Some(ISSUE_TYPE_NAME),
          statusName        = status,
          priorityName      = priority,
          optAssignee       = maybeConvertedAssignee,
          operation         = BacklogOperation(
            optCreatedUser    = Some(convertedCreator),
            optCreated        = Some(DateUtil.toDateTimeString(fromCybozuIssue.todo.createdAt)),
            optUpdatedUser    = Some(convertedUpdater),
            optUpdated        = Some(DateUtil.toDateTimeString(fromCybozuIssue.todo.updatedAt))
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
