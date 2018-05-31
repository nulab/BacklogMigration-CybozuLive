package com.nulabinc.backlog.c2b.services

import java.util.Date

import better.files.File
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.converters._
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import com.nulabinc.backlog.c2b.exceptions.CybozuLiveImporterException
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.migration.common.conf.BacklogPaths
import com.nulabinc.backlog.migration.common.domain._
import com.osinka.i18n.Messages
import spray.json._

object BacklogExport extends Logger {

  import com.nulabinc.backlog.migration.common.domain.BacklogJsonProtocol._

  val start: AppProgram[Unit] = AppDSL.fromConsole(ConsoleDSL.printBold(Messages("export.start")))
  val finish: AppProgram[Unit] = AppDSL.fromConsole(ConsoleDSL.printBold(Messages("export.finish")))

  val issueTypes: Map[IssueType, CybozuIssueType] = Map(
    IssueType.ToDo -> CybozuIssueType(Messages("issue.type.todo")),
    IssueType.Event -> CybozuIssueType(Messages("issue.type.event")),
    IssueType.Forum -> CybozuIssueType(Messages("issue.type.forum"))
  )

  val openStatusName: String = Messages("name.status.open")

  def all(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] =
    for {
      _ <- start
      _ <- project(config)
      _ <- users(config)
      _ <- categories(config)
      _ <- versions(config)
      _ <- exportIssueTypes(config, issueTypes)
      _ <- customFields(config)
      todoCount <- todos(config, issueTypes(IssueType.ToDo), openStatusName, 0)
      eventCount <- events(config, issueTypes(IssueType.Event), todoCount)
      _ <- forums(config, issueTypes(IssueType.Forum), eventCount)
      _ <- finish
    } yield ()

  def project(config: Config): AppProgram[Unit] = {
    val projectResult = ProjectConverter.to(config.projectKey)
    for {
      _ <- projectResult match {
        case Right(project) =>
          AppDSL.export(
            Messages("export.project"),
            config.backlogPaths.projectJson,
            BacklogProjectWrapper(project).toJson.prettyPrint
          )
        case Left(error) =>
          throw CybozuLiveImporterException(error.toString)
      }
    } yield ()
  }

  def users(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._
    import com.nulabinc.backlog.c2b.syntax.AppProgramOps._

    val userConverter = new UserConverter()
    for {
      userStream <- AppDSL.fromStore(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(userStream).orFail
      result = cybozuUsers.map(userConverter.to).sequence
      _ <- result match {
        case Right(backlogUsers) =>
          AppDSL.export(
            Messages("export.user"),
            config.backlogPaths.projectUsersJson,
            BacklogProjectUsersWrapper(backlogUsers).toJson.prettyPrint
          )
        case Left(error) =>
          throw CybozuLiveImporterException("User convert error. " + error.toString)
      }
    } yield ()
  }

  def categories(config: Config): AppProgram[File] =
    AppDSL.export(
      Messages("export.category"),
      config.backlogPaths.issueCategoriesJson,
      BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
    )

  def versions(config: Config): AppProgram[File] =
    AppDSL.export(
      Messages("export.version"),
      config.backlogPaths.versionsJson,
      BacklogVersionsWrapper(Seq.empty[BacklogVersion]).toJson.prettyPrint
    )

  def exportIssueTypes(config: Config, issueTypes: Map[IssueType, CybozuIssueType]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    issueTypes.values.toSeq.map(s => IssueTypeConverter.to(s)).sequence match {
      case Right(backlogIssueTypes) =>
        for {
          _ <- AppDSL.export(
            Messages("export.issue_type"),
            config.backlogPaths.issueTypesJson,
            BacklogIssueTypesWrapper(backlogIssueTypes).toJson.prettyPrint
          )
        } yield ()
      case Left(error) =>
        throw CybozuLiveImporterException(error.toString)
    }
  }

  def customFields(config: Config): AppProgram[File] =
    AppDSL.export(
      Messages("export.custom_field"),
      config.backlogPaths.customFieldSettingsJson,
      BacklogCustomFieldSettingsWrapper(Seq.empty[BacklogCustomFieldSetting]).toJson.prettyPrint
    )

  def todos(config: Config,
            issueType: CybozuIssueType,
            openStatusName: String,
            startIndex: Int)(implicit mappingContext: MappingContext): AppProgram[Int] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new CommentConverter()

    for {
      todos <- AppDSL.fromStore(StoreDSL.getTodos)
      count <- AppDSL.fromStore(StoreDSL.getTodoCount)
      _ <- AppDSL.consumeStream {
        todos.zipWithIndex.map {
          case (todo, index) =>
            exportTodo(config.backlogPaths, todo.id, issueType, issueConverter, commentConverter, openStatusName, startIndex + index, startIndex + count)
        }
      }
    } yield startIndex + count
  }

  def events(config: Config, issueType: CybozuIssueType, startIndex: Int)(implicit mappingContext: MappingContext): AppProgram[Int] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new CommentConverter()

    for {
      events <- AppDSL.fromStore(StoreDSL.getEvents)
      total <- AppDSL.fromStore(StoreDSL.getEventCount)
      _ <- AppDSL.consumeStream {
        events.zipWithIndex.map {
          case (event, index) =>
            exportEvent(config.backlogPaths, event.id, issueType, issueConverter, commentConverter, startIndex + index, startIndex + total)
        }
      }
    } yield startIndex + total
  }

  def forums(config: Config, issueType: CybozuIssueType, startIndex: Int)(implicit mappingContext: MappingContext): AppProgram[Int] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new CommentConverter()

    for {
      forums <- AppDSL.fromStore(StoreDSL.getForums)
      total <- AppDSL.fromStore(StoreDSL.getForumCount)
      _ <- AppDSL.consumeStream {
        forums.zipWithIndex.map {
          case (forum, index) =>
            exportForum(config.backlogPaths, forum.id, issueType, issueConverter, commentConverter, startIndex + index, startIndex + total)
        }
      }
    } yield total
  }

  private def exportTodo(paths: BacklogPaths,
                         todoId: AnyId,
                         issueType: CybozuIssueType,
                         issueConverter: IssueConverter,
                         commentConverter: CommentConverter,
                         openStatusName: String,
                         index: Long,
                         total: Long): AppProgram[Unit] =
    for {
      optTodo <- AppDSL.fromStore(StoreDSL.getTodo(Id.todoId(todoId)))
      _ <- optTodo.map { todo =>
        val newId = index.toInt + 1
        val comments = todo.comments.map(c => c.copy(parentId = newId))
        todo.copy(id = newId, comments = comments)
      }.map { todo =>
        issueConverter.from(todo, issueType) match {
          case Right(backlogIssue) =>
            for {
              _ <- exportIssue(paths, backlogIssue, todo.createdAt, index, total)
              _ <- if (backlogIssue.statusName != openStatusName) {
                val comment = createBacklogCommentWithStatusChangelog(
                  parentIssueId = todo.id,
                  oldStatusName = openStatusName,
                  newStatusName = backlogIssue.statusName,
                  backlogOperation = backlogIssue.operation
                )
                for {
                  _ <- exportComment(paths, todo.id, comment, todo.createdAt, 0)
                  _ <- exportComments(paths, todo.comments, commentConverter, 1)
                } yield ()
              } else {
                exportComments(paths, todo.comments, commentConverter)
              }
            } yield ()
          case Left(error) =>
            throw CybozuLiveImporterException("ToDo convert error. " + error.toString)
        }
      }.getOrElse(throw CybozuLiveImporterException("ToDo not found"))
    } yield ()

  private def exportEvent(paths: BacklogPaths,
                          eventId: AnyId,
                          issueType: CybozuIssueType,
                          issueConverter: IssueConverter,
                          commentConverter: CommentConverter,
                          index: Long,
                          total: Long): AppProgram[Unit] =
    for {
      optEvent <- AppDSL.fromStore(StoreDSL.getEvent(eventId))
      _ <- optEvent.map { event =>
        val newId = index.toInt + 1
        val comments = event.comments.map(c => c.copy(parentId = newId))
        event.copy(id = newId, comments = comments)
      }.map { event =>
        issueConverter.from(event, issueType) match {
          case Right(backlogIssue) if backlogIssue.summary.value.isEmpty =>
            log.warn(s"Event title is empty. Ignored. Id: $eventId Memo: ${event.memo}")
            AppDSL.pure(())
          case Right(backlogIssue) =>
            for {
              _ <- exportIssue(paths, backlogIssue, event.startDateTime, index, total)
              _ <- exportComments(paths, event.comments, commentConverter)
            } yield ()
          case Left(error) =>
            throw CybozuLiveImporterException("Event convert error. " + error.toString)
        }
      }.getOrElse(throw CybozuLiveImporterException("Event not found"))
    } yield ()

  private def exportForum(paths: BacklogPaths,
                          forumId: AnyId,
                          issueType: CybozuIssueType,
                          issueConverter: IssueConverter,
                          commentConverter: CommentConverter,
                          index: Long,
                          total: Long): AppProgram[Unit] =
    for {
      optForum <- AppDSL.fromStore(StoreDSL.getForum(forumId))
      _ <- optForum
        .map { forum =>
          val newId = index.toInt + 1
          val comments = forum.comments.map(c => c.copy(parentId = newId))
          forum.copy(id = newId, comments = comments)
        }.map { forum =>
          issueConverter.from(forum.copy(id = index.toInt + 1), issueType) match {
            case Right(backlogIssue) =>
              for {
                _ <- exportIssue(paths, backlogIssue, forum.createdAt, index, total)
                _ <- exportComments(paths, forum.comments, commentConverter)
              } yield ()
            case Left(error) =>
              throw CybozuLiveImporterException("Forum convert error. " + error.toString)
          }
        }.getOrElse(throw CybozuLiveImporterException("Forum not found"))
    } yield ()


  private def exportIssue(paths: BacklogPaths,
                          backlogIssue: BacklogIssue,
                          createdAt: DateTime,
                          index: Long,
                          total: Long): AppProgram[File] = {
    val issueDirPath = paths.issueDirectoryPath("issue", backlogIssue.id, Date.from(createdAt.toInstant),0)
    AppDSL.export(
      Messages("export.issue", index + 1, total, backlogIssue.summary.value),
      paths.issueJson(issueDirPath),
      backlogIssue.toJson.prettyPrint
    )
  }

  private def exportComment(paths: BacklogPaths,
                            issueId: AnyId,
                            backlogComment: BacklogComment,
                            commentCreatedAt: DateTime,
                            index: Int): AppProgram[Unit] = {
    val createdDate = Date.from(commentCreatedAt.toInstant)
    val issueDirPath = paths.issueDirectoryPath("comment", issueId, createdDate, index)
    AppDSL.export(
      "  " + Messages("export.comment"),
      paths.issueJson(issueDirPath),
      backlogComment.toJson.prettyPrint
    ).map(_ => ())
  }

  private def exportComment(paths: BacklogPaths,
                            cybozuComment: CybozuComment,
                            index: Int,
                            converter: CommentConverter): AppProgram[Unit] = {
    converter.to(cybozuComment) match {
      case Right(backlogComment) =>
        exportComment(paths, cybozuComment.parentId, backlogComment, cybozuComment.createdAt, index)
      case Left(error) =>
        throw CybozuLiveImporterException("Comment convert error. " + error.toString)
    }
  }

  private def exportComments(paths: BacklogPaths,
                             comments: Seq[CybozuComment],
                             converter: CommentConverter,
                             offset: Int = 0): AppProgram[Seq[Unit]] = {
    import com.nulabinc.backlog.c2b.syntax.AppProgramOps._
    
    comments.zipWithIndex.map {
      case (cybozuComment, index) =>
        exportComment(paths, cybozuComment, index + offset, converter)
    }.sequence
  }

  private def createBacklogCommentWithStatusChangelog(parentIssueId: Long,
                                                      oldStatusName: String,
                                                      newStatusName: String,
                                                      backlogOperation: BacklogOperation): BacklogComment =
    BacklogComment(
      eventType = "comment",
      optIssueId = Some(parentIssueId),
      optContent = None,
      changeLogs = Seq(
        BacklogChangeLog(
          field = "status",
          optOriginalValue = Some(oldStatusName),
          optNewValue = Some(newStatusName),
          optAttachmentInfo = None,
          optAttributeInfo = None,
          optNotificationInfo = None
        )
      ),
      notifications = Seq(),
      optCreatedUser = backlogOperation.optCreatedUser,
      optCreated =  backlogOperation.optCreated
    )
}
