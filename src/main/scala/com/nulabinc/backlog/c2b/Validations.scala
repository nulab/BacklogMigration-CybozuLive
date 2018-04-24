package com.nulabinc.backlog.c2b

import java.nio.file.Path

import cats.free.Free
import com.github.chaabaj.backlog4s.apis._
import com.github.chaabaj.backlog4s.datas._
import com.github.chaabaj.backlog4s.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppADT, AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL
import com.nulabinc.backlog.c2b.services.MappingFiles
import com.osinka.i18n.Messages

import scala.util.{Failure, Success, Try}

object Validations extends Logger {

  private val backlogName = Messages("name.backlog")
  private val cybozuName = Messages("name.cybozu")
  private val userMappingName = Messages("name.mapping.user")
  private val priorityMappingName = Messages("name.mapping.priority")
  private val statusMappingName = Messages("name.mapping.status")

  def checkBacklog(config: Config, spaceApi: SpaceApi): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.interpreters.AppDSL._
    import com.nulabinc.backlog.c2b.syntax.BacklogResponseOps._

    for {
      // Access check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.access", backlogName)))
      apiAccess <- fromBacklog(spaceApi.logo)
      _ <- apiAccess.orExit(
        Messages("validation.access.ok", backlogName),
        Messages("validation.access.error", backlogName)
      )
      // Admin check
      _ <- fromConsole(ConsoleDSL.print(Messages("validation.admin", backlogName)))
      adminCheck <- fromBacklog(spaceApi.diskUsage)
      _ <- adminCheck.orExit(
        Messages("validation.admin.ok", backlogName),
        Messages("validation.admin.error", backlogName)
      )
    } yield ()
  }

  def projectExists(config: Config, projectApi: ProjectApi): AppProgram[Unit] = {
    for {
      exists <- backlogProjectExists(projectApi, config.projectKey)
      _ <- exists match {
        case Right(_) =>
          for {
            input <- AppDSL.fromConsole(ConsoleDSL.read(Messages("validation.backlog_project_already_exist", config.projectKey)))
            _ <- if(input == "y" || input == "Y")
              AppDSL.pure(())
            else
              AppDSL.exit(Messages("message.import.cancel"), 0)
          } yield ()
        case Left(_) =>
          AppDSL.pure(())
      }
    } yield ()
  }

  def checkDBExists(dbPath: Path): AppProgram[Unit] = {
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

  def checkMappingFilesExist(config: Config): AppProgram[Unit] =
    for {
      _ <- checkUserMappingFileExists(config.USERS_PATH)
      _ <- checkPriorityMappingFileExists(config.PRIORITIES_PATH)
      _ <- checkStatusMappingFileExists(config.STATUSES_PATH)
    } yield ()

  def checkMappingFilesCSVFormat(config: Config): AppProgram[Unit] = ???

  def checkMappingFileCSVFormat(path: Path): AppProgram[Unit] = {
    for {
      stream <- MappingFiles.read(path)
      result <- AppDSL.streamAsSeq(stream)
      _ <- result match {
        case Success(_) =>
          AppDSL.empty
        case Failure(ex) =>
          ex.printStackTrace()
          AppDSL.exit("cannot parse", 1)
      }
    } yield ()
  }

  def checkMappingFileItems(config: Config, api: AllApi): AppProgram[Unit] =
    for {
      _ <- userMappingFileItems(api.userApi, config)
      _ <- priorityMappingFileItems(api.priorityApi, config)
      _ <- statusMappingFileItems(api.statusApi, config)
    } yield ()

  private def checkUserMappingFileExists(path: Path): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists")))
      userExists <- mappingFileExists(path)
      _ <- if (userExists)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", userMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", userMappingName), 1)
    } yield ()

  private def checkPriorityMappingFileExists(path: Path): AppProgram[Unit] =
    for {
      priority <- AppDSL.fromStorage(StorageDSL.exists(path))
      _ <- if (priority)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", priorityMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", priorityMappingName), 1)
    } yield ()

  private def checkStatusMappingFileExists(path: Path): AppProgram[Unit] =
    for {
      status <- AppDSL.fromStorage(StorageDSL.exists(path))
      _ <- if (status)
        AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists.ok", statusMappingName)))
      else
        AppDSL.exit(Messages("validation.mapping.file.exists.error", statusMappingName), 1)
    } yield ()

  private def userMappingFileItems(api: UserApi, config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.item.exists", userMappingName)))
      usersResult <- AppDSL.fromBacklog(api.all)
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

  private def backlogProjectExists(projectApi: ProjectApi, projectKey: String): AppProgram[Response[Project]] =
    AppDSL.fromBacklog(
      projectApi.byIdOrKey(
        KeyParam[Project](
          Key[Project](projectKey)
        )
      )
    )

  private def mappingFileExists(path: Path): AppProgram[Boolean] =
    AppDSL.fromStorage(StorageDSL.exists(path))

}
