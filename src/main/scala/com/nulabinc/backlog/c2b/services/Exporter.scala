package com.nulabinc.backlog.c2b.services

import com.nulabinc.backlog.c2b.CybozuBacklogPaths
import com.nulabinc.backlog.c2b.converters.{BacklogIssueTypeConverter, BacklogProjectConverter}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
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

  private def backlogPath(projectKey: String): CybozuBacklogPaths =
    new CybozuBacklogPaths(projectKey)

}
