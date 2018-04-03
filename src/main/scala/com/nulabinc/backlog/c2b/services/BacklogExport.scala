package com.nulabinc.backlog.c2b.services

import java.util.Date

import better.files.File
import com.nulabinc.backlog.c2b.CybozuBacklogPaths
import com.nulabinc.backlog.c2b.converters.{BacklogCommentConverter, BacklogIssueTypeConverter, BacklogProjectConverter, IssueConverter}
import com.nulabinc.backlog.c2b.datas.{CybozuComment, CybozuDBComment, MappingContext}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.migration.common.domain._
import spray.json._

object BacklogExport {

  import com.nulabinc.backlog.migration.common.domain.BacklogJsonProtocol._

  def project(projectKey: String): AppProgram[Unit] = {
    val projectResult = BacklogProjectConverter.to(projectKey)
    for {
      _ <- projectResult match {
        case Right(project) =>
          AppDSL.export(backlogPath(projectKey).projectJson, BacklogProjectWrapper(project).toJson.prettyPrint)
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()
  }

  def categories(projectKey: String): AppProgram[File] =
    AppDSL.export(
      backlogPath(projectKey).issueCategoriesJson,
      BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
    )

  def versions(projectKey: String): AppProgram[File] =
    AppDSL.export(
      backlogPath(projectKey).versionsJson,
      BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
    )

  def issueTypes(projectKey: String, issueTypes: Seq[String]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    issueTypes.map(s => BacklogIssueTypeConverter.to(s)).sequence match {
      case Right(backlogIssueTypes) =>
        for {
          _ <- AppDSL.export(
            backlogPath(projectKey).issueTypesJson,
            BacklogIssueTypesWrapper(backlogIssueTypes).toJson.prettyPrint
          )
        } yield ()
      case Left(error) =>
        AppDSL.exit(error.toString, 1)
    }
  }

  def customFields(projectKey: String): AppProgram[File] =
    AppDSL.export(
      backlogPath(projectKey).customFieldSettingsJson,
      BacklogCustomFieldSettingsWrapper(Seq.empty[BacklogCustomFieldSetting]).toJson.prettyPrint
    )

  private def exportIssue(paths: CybozuBacklogPaths, backlogIssue: BacklogIssue, createdAt: DateTime): AppProgram[File] = {
    val issueDirPath = paths.issueDirectoryPath("issue", backlogIssue.id, Date.from(createdAt.toInstant),0)
    AppDSL.export(paths.issueJson(issueDirPath), backlogIssue.toJson.prettyPrint)
  }

  private def exportComments(paths: CybozuBacklogPaths,
                             issueId: AnyId,
                             comments: Seq[CybozuComment],
                             converter: BacklogCommentConverter): AppProgram[Seq[Unit]] = {
    val prgs = comments.zipWithIndex.map {
      case (cybozuComment, index) =>
        val createdDate = Date.from(comments(index).comment.createdAt.toInstant)
        val issueDirPath = paths.issueDirectoryPath("comment", issueId, createdDate, index)

        converter.from(cybozuComment) match {
          case Right(backlogComment) =>
            val createdDate = Date.from(comments(index).comment.createdAt.toInstant)
            val issueDirPath = paths.issueDirectoryPath("comment", issueId, createdDate, index)
            AppDSL.export(paths.issueJson(issueDirPath), backlogComment.toJson.prettyPrint).map(_ => ())
          case Left(error) =>
            AppDSL.exit("Comment convert error. " + error.toString, 1)
        }
    }
    sequence(prgs)
  }

  private def exportTodo(paths: CybozuBacklogPaths,
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

  def issues(projectKey: String)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val issueConverter = new IssueConverter()
    val commentConverter = new BacklogCommentConverter()
    val backlogPaths = backlogPath(projectKey)

    for {
      todos <- AppDSL.fromDB(StoreDSL.getTodos)
      _ <- AppDSL.consumeStream {
        todos.map(todo => exportTodo(backlogPaths, todo.id, issueConverter, commentConverter))
      }
    } yield ()
  }

  private def backlogPath(projectKey: String): CybozuBacklogPaths =
    new CybozuBacklogPaths(projectKey)

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
