package com.nulabinc.backlog.c2b.services

import java.util.Date

import better.files.File
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.converters.{BacklogCommentConverter, BacklogIssueTypeConverter, BacklogProjectConverter, IssueConverter}
import com.nulabinc.backlog.c2b.datas.{CybozuComment, MappingContext}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.migration.common.conf.BacklogPaths
import com.nulabinc.backlog.migration.common.domain._
import spray.json._

object BacklogExport {

  import com.nulabinc.backlog.migration.common.domain.BacklogJsonProtocol._

  def all(config: Config, cybozuIssueTypes: Seq[String])(implicit mappingContext: MappingContext): AppProgram[Unit] =
    for {
      _ <- project(config)
      _ <- categories(config)
      _ <- versions(config)
      _ <- issueTypes(config, cybozuIssueTypes)
      _ <- customFields(config)
      _ <- todos(config)
      _ <- events(config)
      _ <- forums(config)
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

  def issueTypes(config: Config, issueTypes: Seq[String]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    issueTypes.map(s => BacklogIssueTypeConverter.to(s)).sequence match {
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

  def todos(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      todos <- AppDSL.fromDB(StoreDSL.getTodos)
      _ <- AppDSL.consumeStream {
        todos.map(todo => exportTodo(config.backlogPaths, todo.id, issueConverter, commentConverter))
      }
    } yield ()
  }

  def events(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      events <- AppDSL.fromDB(StoreDSL.getEvents)
      _ <- AppDSL.consumeStream {
        events.map(event => exportEvent(config.backlogPaths, event.id, issueConverter, commentConverter))
      }
    } yield ()
  }

  def forums(config: Config)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()

    for {
      forums <- AppDSL.fromDB(StoreDSL.getForums)
      _ <- AppDSL.consumeStream {
        forums.map(forum => exportForum(config.backlogPaths, forum.id, issueConverter, commentConverter))
      }
    } yield ()
  }

  private def exportTodo(paths: BacklogPaths,
                         todoId: AnyId,
                         issueConverter: IssueConverter,
                         commentConverter: BacklogCommentConverter): AppProgram[Unit] =
    for {
      optTodo <- AppDSL.fromDB(StoreDSL.getTodo(todoId))
      _ <- optTodo match {
        case Some(todo) =>
          issueConverter.from(todo) match {
            case Right(backlogIssue) =>
              for {
                _ <- exportIssue(paths, backlogIssue, todo.todo.createdAt)
                _ <- exportComments(paths, todo.todo.id, todo.comments, commentConverter)
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
                          issueConverter: IssueConverter,
                          commentConverter: BacklogCommentConverter): AppProgram[Unit] =
    for {
      optEvent <- AppDSL.fromDB(StoreDSL.getEvent(eventId))
      _ <- optEvent match {
        case Some(event) =>
          issueConverter.from(event) match {
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
                          issueConverter: IssueConverter,
                          commentConverter: BacklogCommentConverter): AppProgram[Unit] =
    for {
      optForum <- AppDSL.fromDB(StoreDSL.getForum(forumId))
      _ <- optForum match {
        case Some(forum) =>
          issueConverter.from(forum) match {
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

  private def exportComments(paths: BacklogPaths,
                             issueId: AnyId,
                             comments: Seq[CybozuComment],
                             converter: BacklogCommentConverter): AppProgram[Seq[Unit]] = {
    val programs = comments.zipWithIndex.map {
      case (cybozuComment, index) =>
        converter.from(issueId, cybozuComment) match {
          case Right(backlogComment) =>
            val createdDate = Date.from(comments(index).comment.createdAt.toInstant)
            val issueDirPath = paths.issueDirectoryPath("comment", issueId, createdDate, index)
            AppDSL.export(paths.issueJson(issueDirPath), backlogComment.toJson.prettyPrint).map(_ => ())
          case Left(error) =>
            AppDSL.exit("Comment convert error. " + error.toString, 1)
        }
    }
    sequence(programs)
  }



  private def sequence[A](prgs: Seq[AppProgram[A]]): AppProgram[Seq[A]] =
    prgs.foldLeft(AppDSL.pure(Seq.empty[A])) {
      case (newPrg, prg) =>
        newPrg.flatMap { results =>
          prg.map { result =>
            results :+ result
          }
        }
    }

}
