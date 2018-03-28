package com.nulabinc.backlog.c2b

import java.nio.file.Path

import backlog4s.apis.AllApi
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL
import com.osinka.i18n.Messages

object Validations extends Logger {

  def backlogProgram(config: Config, backlogApi: AllApi): AppProgram[Unit] = {

    import com.nulabinc.backlog.c2b.interpreters.AppDSL._
    import com.nulabinc.backlog.c2b.interpreters.syntax._

    for {
      // Access check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.access", Messages("name.backlog"))))
      apiAccess <- fromBacklog(backlogApi.spaceApi.logo)
      _ <- apiAccess.orExit(
        Messages("validation.access.ok", Messages("name.backlog")),
        Messages("validation.access.error", Messages("name.backlog"))
      )
      // Admin check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.admin", Messages("name.backlog"))))
      adminCheck <- fromBacklog(backlogApi.spaceApi.diskUsage)
      _ <- adminCheck.orExit(
        Messages("validation.admin.ok", Messages("name.backlog")),
        Messages("validation.admin.error", Messages("name.backlog"))
      )
    } yield ()
  }

  def dbExistsProgram(dbPath: Path): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.db.exists")))
      exists <- AppDSL.fromStorage(StorageDSL.exists(dbPath))
      _ <- if (exists) {
        AppDSL.fromConsole(ConsoleDSL.print("validation.db.exists.ok"))
      } else {
        AppDSL.exit(Messages("validation.db.exists.error"), 1)
      }
    } yield ()
  }

  def mappingFilesExistProgram(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.exists")))
      user <- AppDSL.fromStorage(StorageDSL.exists(config.USERS_PATH))
      _ <- if (user)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.exists.ok", Messages("name.mapping.user"))))
      else
        AppDSL.exit(Messages("validation.mapping.exists.error", Messages("name.mapping.user")), 1)
      priority <- AppDSL.fromStorage(StorageDSL.exists(config.PRIORITIES_PATH))
      _ <- if (priority)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.exists.ok", Messages("name.mapping.priority"))))
      else
        AppDSL.exit(Messages("validation.mapping.exists.error", Messages("name.mapping.priority")), 1)
      status <- AppDSL.fromStorage(StorageDSL.exists(config.STATUSES_PATH))
      _ <- if (status)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.exists.ok", Messages("name.mapping.status"))))
      else
        AppDSL.exit(Messages("validation.mapping.exists.error", Messages("name.mapping.status")), 1)
    } yield ()
  }

}
