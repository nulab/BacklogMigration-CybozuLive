package com.nulabinc.backlog.c2b.services

import java.util.Date

import better.files.File
import com.nulabinc.backlog.c2b.CybozuBacklogPaths
import com.nulabinc.backlog.c2b.converters.{BacklogIssueTypeConverter, BacklogProjectConverter, IssueConverter}
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.datas.Types.AnyId
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

  private def exportTodo(projectKey: String,
                         todoId: AnyId,
                         converter: IssueConverter): AppProgram[Unit] =
    for {
      optTodo <- AppDSL.fromDB(StoreDSL.getTodo(todoId))
      _ <- optTodo match {
        case Some(todo) =>
          converter.from(todo) match {
            case Right(backlogIssue) =>
              val issueDirPath = backlogPath(projectKey).issueDirectoryPath(
                "issue",
                backlogIssue.id,
                Date.from(todo.todo.createdAt.toInstant),
                0
              )
              AppDSL.export(backlogPath(projectKey).issueJson(issueDirPath), backlogIssue.toJson.prettyPrint)
            case Left(error) =>
              AppDSL.exit("ToDo convert error. " + error.toString, 1)
          }
        case None =>
          AppDSL.exit("ToDo not found", 1)
      }
    } yield ()

  def issues(projectKey: String)(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    val converter = new IssueConverter()

    for {
      todos <- AppDSL.fromDB(StoreDSL.getTodos)
      _ <- AppDSL.consumeStream {
        todos.map(todo => exportTodo(projectKey, todo.id, converter))
      }
    } yield ()
  }

  private def backlogPath(projectKey: String): CybozuBacklogPaths =
    new CybozuBacklogPaths(projectKey)

}
