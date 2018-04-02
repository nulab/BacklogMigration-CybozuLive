package com.nulabinc.backlog.c2b.services

import java.util.Date

import com.nulabinc.backlog.c2b.CybozuBacklogPaths
import com.nulabinc.backlog.c2b.converters.{BacklogIssueTypeConverter, BacklogProjectConverter, IssueConverter}
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.migration.common.domain._
import com.nulabinc.backlog.migration.common.utils.IOUtil
import spray.json._

object Exporter {

  import com.nulabinc.backlog.migration.common.domain.BacklogJsonProtocol._

  def project(projectKey: String): AppProgram[Unit] = {
    val backlogPaths = new CybozuBacklogPaths(projectKey)
    val projectResult = BacklogProjectConverter.to(projectKey)
    for {
      _ <- projectResult match {
        case Right(project) => AppDSL.pure(
          IOUtil.output(backlogPaths.projectJson, BacklogProjectWrapper(project).toJson.prettyPrint)
        )
        case Left(error) => AppDSL.exit(error.toString, 1)
      }
    } yield ()
  }

  def categories(projectKey: String): AppProgram[Unit] = {
    AppDSL.pure(
      IOUtil.output(
        backlogPath(projectKey).issueCategoriesJson,
        BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
      )
    )
  }

  def versions(projectKey: String): AppProgram[Unit] =
    AppDSL.pure(
      IOUtil.output(
        backlogPath(projectKey).versionsJson,
        BacklogIssueCategoriesWrapper(Seq.empty[BacklogIssueCategory]).toJson.prettyPrint
      )
    )

  def issueTypes(projectKey: String, issueTypes: Seq[String]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    issueTypes.map(s => BacklogIssueTypeConverter.to(s)).sequence match {
      case Right(backlogIssueTypes) =>
        AppDSL.pure(
          IOUtil.output(
            backlogPath(projectKey).issueTypesJson,
            BacklogIssueTypesWrapper(backlogIssueTypes).toJson.prettyPrint
          )
        )
      case Left(error) => AppDSL.exit(error.toString, 1)
    }
  }

  def customFields(projectKey: String): AppProgram[Unit] =
    AppDSL.pure(
      IOUtil.output(
        backlogPath(projectKey).customFieldSettingsJson,
        BacklogCustomFieldSettingsWrapper(Seq.empty[BacklogCustomFieldSetting]).toJson.prettyPrint
      )
    )

  def isuses(projectKey: String)(implicit mappingContext: MappingContext): AppProgram[Unit] = {

    val converter = new IssueConverter()

    for {
      todos <- AppDSL.fromDB(StoreDSL.getTodos)
      _ <- AppDSL.consumeStream {
        todos.map { dbTodo =>
          for {
            optTodo <- AppDSL.fromDB(StoreDSL.getTodo(dbTodo.id))
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
                    AppDSL.pure(
                      IOUtil.output(backlogPath(projectKey).issueJson(issueDirPath), backlogIssue.toJson.prettyPrint)
                    )
                  case Left(error) =>
                    AppDSL.exit("ToDo convert error. " + error.toString, 1)
                }
              case None =>
                AppDSL.exit("ToDo not found", 1)
            }
          } yield ()
        }
      }
    } yield ()
  }

  private def backlogPath(projectKey: String): CybozuBacklogPaths =
    new CybozuBacklogPaths(projectKey)

}
