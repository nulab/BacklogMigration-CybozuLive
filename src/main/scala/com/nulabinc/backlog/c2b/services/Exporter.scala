package com.nulabinc.backlog.c2b.services

import com.nulabinc.backlog.c2b.CybozuBacklogPaths
import com.nulabinc.backlog.c2b.converters.BacklogProjectConverter
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.migration.common.domain.BacklogProjectWrapper
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

}
