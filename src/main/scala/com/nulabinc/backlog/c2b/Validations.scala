package com.nulabinc.backlog.c2b

import java.nio.file.Path

import backlog4s.apis.{AllApi, PriorityApi, StatusApi, UserApi}
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL
import com.nulabinc.backlog.c2b.services.MappingFiles
import com.osinka.i18n.Messages

object Validations extends Logger {

  private val backlogName = Messages("name.backlog")
  private val cybozuName = Messages("name.cybozu")
  private val userMappingName = Messages("name.mapping.user")
  private val priorityMappingName = Messages("name.mapping.priority")
  private val statusMappingName = Messages("name.mapping.status")

  def backlogProgram(config: Config, backlogApi: AllApi): AppProgram[Unit] = {

    import com.nulabinc.backlog.c2b.interpreters.AppDSL._
    import com.nulabinc.backlog.c2b.syntax.BacklogResponseOps._

    for {
      // Access check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.access", backlogName)))
      apiAccess <- fromBacklog(backlogApi.spaceApi.logo)
      _ <- apiAccess.orExit(
        Messages("validation.access.ok", backlogName),
        Messages("validation.access.error", backlogName)
      )
      // Admin check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.admin", backlogName)))
      adminCheck <- fromBacklog(backlogApi.spaceApi.diskUsage)
      _ <- adminCheck.orExit(
        Messages("validation.admin.ok", backlogName),
        Messages("validation.admin.error", backlogName)
      )
    } yield ()
  }

  def dbExistsProgram(dbPath: Path): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.db.exists")))
      exists <- AppDSL.fromStorage(StorageDSL.exists(dbPath))
      _ <- if (exists) {
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.db.exists.ok")))
      } else {
        AppDSL.exit(Messages("validation.db.exists.error"), 1)
      }
    } yield ()
  }

  def mappingFilesExistProgram(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists")))
      user <- AppDSL.fromStorage(StorageDSL.exists(config.USERS_PATH))
      _ <- if (user)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", userMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", userMappingName), 1)
      priority <- AppDSL.fromStorage(StorageDSL.exists(config.PRIORITIES_PATH))
      _ <- if (priority)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", priorityMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", priorityMappingName), 1)
      status <- AppDSL.fromStorage(StorageDSL.exists(config.STATUSES_PATH))
      _ <- if (status)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", statusMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", statusMappingName), 1)
    } yield ()
  }

  def mappingFileItems(api: AllApi, config: Config): AppProgram[Unit] =
    for {
      _ <- userMappingFileItems(api.userApi, config)
      _ <- priorityMappingFileItems(api.priorityApi, config)
      _ <- statusMappingFileItems(api.statusApi, config)
    } yield ()

  private def userMappingFileItems(api: UserApi, config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.item.exists", userMappingName)))
      usersResult <- AppDSL.fromBacklog(api.all())
      _ <- usersResult match {
        case Right(users) =>
          for {
            userMappings <- MappingFiles.read(config.USERS_PATH)
            _ <- AppDSL.consumeStream(
              userMappings.map {
                case (cybozu, backlog) if backlog.isEmpty =>
                  AppDSL.exit(Messages("validation.mapping.item.empty", backlogName, userMappingName, cybozuName, cybozu), 1)
                case (cybozu, backlog) =>
                  val userExists = users.exists(_.userId.contains(backlog))
                  if (userExists) {
                    AppDSL.pure(())
                  } else {
                    AppDSL.exit(Messages("validation.mapping.item.error", backlogName, userMappingName, cybozuName, cybozu, backlog), 1)
                  }
              }
            )
          } yield ()
        case Left(error) =>
          AppDSL.exit("Get backlog users fail. " + error.toString, 1)
      }
    } yield ()

  private def priorityMappingFileItems(api: PriorityApi, config: Config): AppProgram[Unit] =
    for {
      result <- AppDSL.fromBacklog(api.all)
      _ <- result match {
        case Right(priorities) =>
          for {
            mappings <- MappingFiles.read(config.PRIORITIES_PATH)
            _ <- AppDSL.consumeStream(
              mappings.map {
                case (cybozu, backlog) if backlog.isEmpty =>
                  AppDSL.exit(Messages("validation.mapping.item.empty", backlogName, priorityMappingName, cybozuName, cybozu), 1)
                case (cybozu, backlog) =>
                  val exists = priorities.exists(_.name == backlog)
                  if (exists) {
                    AppDSL.pure(())
                  } else {
                    AppDSL.exit(Messages("validation.mapping.item.error", backlogName, priorityMappingName, cybozuName, cybozu, backlog), 1)
                  }
              }
            )
          } yield ()
        case Left(error) =>
          AppDSL.exit("Get backlog priorities fail. " + error.toString, 1)
      }
    } yield ()

  private def statusMappingFileItems(api: StatusApi, config: Config): AppProgram[Unit] =
    for {
      result <- AppDSL.fromBacklog(api.all)
      _ <- result match {
        case Right(statuses) =>
          for {
            mappings <- MappingFiles.read(config.STATUSES_PATH)
            _ <- AppDSL.consumeStream(
              mappings.map {
                case (cybozu, backlog) if backlog.isEmpty =>
                  AppDSL.exit(Messages("validation.mapping.item.empty", backlogName, statusMappingName, cybozuName, cybozu), 1)
                case (cybozu, backlog) =>
                  val exists = statuses.exists(_.name == backlog)
                  if (exists) {
                    AppDSL.pure(())
                  } else {
                    AppDSL.exit(Messages("validation.mapping.item.error", backlogName, statusMappingName, cybozuName, cybozu, backlog), 1)
                  }
              }
            )
          } yield ()
        case Left(error) =>
          AppDSL.exit("Get backlog statuses fail. " + error.toString, 1)
      }
    } yield ()


}
