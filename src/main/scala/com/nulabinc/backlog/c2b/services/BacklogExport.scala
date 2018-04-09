package com.nulabinc.backlog.c2b.services

import java.util.Date

import better.files.File
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.converters._
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.migration.common.conf.BacklogPaths
import com.nulabinc.backlog.migration.common.domain._
import spray.json._

object BacklogExport {

  import com.nulabinc.backlog.migration.common.domain.BacklogJsonProtocol._

  def all(config: Config,
          issueTypes: Map[IssueType, CybozuIssueType],
          openStatusName: String)(implicit mappingContext: MappingContext): AppProgram[Unit] =
    for {
      _ <- project(config)
      _ <- users(config)
      _ <- categories(config)
      _ <- versions(config)
      _ <- exportIssueTypes(config, issueTypes)
      _ <- customFields(config)
      _ <- todos(config, issueTypes(IssueType.ToDo), openStatusName)
      _ <- events(config, issueTypes(IssueType.Event))
      _ <- forums(config, issueTypes(IssueType.Forum))
    } yield ()

  def project(config: Config): AppProgram[Unit] = {
    val projectResult = BacklogProjectConverter.to(config.projectKey)
    for {
      _ <- projectResult match {
        case Right(project) =>
          AppDSL.export(config.backlogPaths.projectJson, BacklogProjectWrapper(project).toJson.prettyPrint)
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()
  }

  def users(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    val userConverter = new BacklogUserConverter()
    for {
      userStream <- AppDSL.fromDB(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(userStream)
      result = cybozuUsers.map(userConverter.to).sequence
      _ <- result match {
        case Right(backlogUsers) =>
          AppDSL.export(
            config.backlogPaths.projectUsersJson,
            BacklogProjectUsersWrapper(backlogUsers).toJson.prettyPrint
          )
        case Left(error) =>
          AppDSL.exit("User convert error. " + error.toString, 1)
      }
    } yield ()
  }

  def categories(config: Config): AppProgram[File] =
    AppDSL.export(
      config.backlogPaths.issueCategoriesJson,
      BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
    )

  def versions(config: Config): AppProgram[File] =
    AppDSL.export(
      config.backlogPaths.versionsJson,
      BacklogVersionsWrapper(Seq.empty[BacklogVersion]).toJson.prettyPrint
    )

  def exportIssueTypes(config: Config, issueTypes: Map[IssueType, CybozuIssueType]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    issueTypes.values.toSeq.map(s => BacklogIssueTypeConverter.to(s)).sequence match {
      case Right(backlogIssueTypes) =>
        for {
          _ <- AppDSL.export(
            config.backlogPaths.issueTypesJson,
            BacklogIssueTypesWrapper(backlogIssueTypes).toJson.prettyPrint
          )
        } yield ()
      case Left(error) =>
        AppDSL.exit(error.toString, 1)
    }
  }

  def customFields(config: Config): AppProgram[File] =
    AppDSL.export(
      config.backlogPaths.customFieldSettingsJson,
      BacklogCustomFieldSettingsWrapper(Seq.empty[BacklogCustomFieldSetting]).toJson.prettyPrint
    )

  def todos(config: Config,
            issueType: CybozuIssueType,
            openStatusName: String)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      todos <- AppDSL.fromDB(StoreDSL.getTodos)
      _ <- AppDSL.consumeStream {
        todos.map(todo =>
          exportTodo(config.backlogPaths, todo.id, issueType, issueConverter, commentConverter, openStatusName)
        )
      }
    } yield ()
  }

  def events(config: Config, issueType: CybozuIssueType)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      events <- AppDSL.fromDB(StoreDSL.getEvents)
      _ <- AppDSL.consumeStream {
        events.map(event => exportEvent(config.backlogPaths, event.id, issueType, issueConverter, commentConverter))
      }
    } yield ()
  }

  def forums(config: Config, issueType: CybozuIssueType)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      forums <- AppDSL.fromDB(StoreDSL.getForums)
      _ <- AppDSL.consumeStream {
        forums.map(forum => exportForum(config.backlogPaths, forum.id, issueType, issueConverter, commentConverter))
      }
    } yield ()
  }

  private def exportTodo(paths: BacklogPaths,
                         todoId: AnyId,
                         issueType: CybozuIssueType,
                         issueConverter: IssueConverter,
                         commentConverter: BacklogCommentConverter,
                         openStatusName: String): AppProgram[Unit] =
    for {
      optTodo <- AppDSL.fromDB(StoreDSL.getTodo(todoId))
      _ <- optTodo match {
        case Some(todo) =>
          issueConverter.from(todo, issueType) match {
            case Right(backlogIssue) =>
              for {
                _ <- exportIssue(paths, backlogIssue, todo.todo.createdAt)
                _ <- if (backlogIssue.statusName != openStatusName) {
                  val comment = createBacklogCommentWithStatusChangelog(
                    parentIssueId = todo.todo.id,
                    oldStatusName = openStatusName,
                    newStatusName = backlogIssue.statusName,
                    backlogOperation = backlogIssue.operation
                  )
                  for {
                    _ <- exportComment(paths, todo.todo.id, comment, todo.todo.createdAt, 0)
                    _ <- exportComments(paths, todo.todo.id, todo.comments, commentConverter, 1)
                  } yield ()
                } else {
                  exportComments(paths, todo.todo.id, todo.comments, commentConverter)
                }
              } yield ()
            case Left(error) =>
              AppDSL.exit("ToDo convert error. " + error.toString, 1)
          }
        case None =>
          AppDSL.exit("ToDo not found", 1)
      }
    } yield ()

  private def exportEvent(paths: BacklogPaths,
                          eventId: AnyId,
                          issueType: CybozuIssueType,
                          issueConverter: IssueConverter,
                          commentConverter: BacklogCommentConverter): AppProgram[Unit] =
    for {
      optEvent <- AppDSL.fromDB(StoreDSL.getEvent(eventId))
      _ <- optEvent match {
        case Some(event) =>
          issueConverter.from(event, issueType) match {
            case Right(backlogIssue) =>
              for {
                _ <- exportIssue(paths, backlogIssue, event.event.startDateTime)
                _ <- exportComments(paths, event.event.id, event.comments, commentConverter)
              } yield ()
            case Left(error) =>
              AppDSL.exit("Event convert error. " + error.toString, 1)
          }
        case None =>
          AppDSL.exit("Event not found", 1)
      }
    } yield ()

  private def exportForum(paths: BacklogPaths,
                          forumId: AnyId,
                          issueType: CybozuIssueType,
                          issueConverter: IssueConverter,
                          commentConverter: BacklogCommentConverter): AppProgram[Unit] =
    for {
      optForum <- AppDSL.fromDB(StoreDSL.getForum(forumId))
      _ <- optForum match {
        case Some(forum) =>
          issueConverter.from(forum, issueType) match {
            case Right(backlogIssue) =>
              for {
                _ <- exportIssue(paths, backlogIssue, forum.forum.createdAt)
                _ <- exportComments(paths, forum.forum.id, forum.comments, commentConverter)
              } yield ()
            case Left(error) =>
              AppDSL.exit("Forum convert error. " + error.toString, 1)
          }
        case None =>
          AppDSL.exit("Forum not found", 1)
      }
    } yield ()


  private def exportIssue(paths: BacklogPaths, backlogIssue: BacklogIssue, createdAt: DateTime): AppProgram[File] = {
    val issueDirPath = paths.issueDirectoryPath("issue", backlogIssue.id, Date.from(createdAt.toInstant),0)
    AppDSL.export(paths.issueJson(issueDirPath), backlogIssue.toJson.prettyPrint)
  }

  private def exportComment(paths: BacklogPaths,
                            issueId: AnyId,
                            backlogComment: BacklogComment,
                            commentCreatedAt: DateTime,
                            index: Int): AppProgram[Unit] = {
    val createdDate = Date.from(commentCreatedAt.toInstant)
    val issueDirPath = paths.issueDirectoryPath("comment", issueId, createdDate, index)
    AppDSL.export(paths.issueJson(issueDirPath), backlogComment.toJson.prettyPrint).map(_ => ())
  }

  private def exportComment(paths: BacklogPaths,
                            issueId: AnyId,
                            cybozuComment: CybozuComment,
                            index: Int,
                            converter: BacklogCommentConverter): AppProgram[Unit] = {
    converter.from(issueId, cybozuComment) match {
      case Right(backlogComment) =>
        exportComment(paths, issueId, backlogComment, cybozuComment.comment.createdAt, index)
      case Left(error) =>
        AppDSL.exit("Comment convert error. " + error.toString, 1)
    }
  }

  private def exportComments(paths: BacklogPaths,
                             issueId: AnyId,
                             comments: Seq[CybozuComment],
                             converter: BacklogCommentConverter,
                             offset: Int = 0): AppProgram[Seq[Unit]] = {
    import com.nulabinc.backlog.c2b.syntax.AppProgramOps._
    
    comments.zipWithIndex.map {
      case (cybozuComment, index) =>
        exportComment(paths, issueId, cybozuComment, index + offset, converter)
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
