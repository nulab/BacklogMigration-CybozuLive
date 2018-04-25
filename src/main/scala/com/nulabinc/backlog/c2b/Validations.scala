package com.nulabinc.backlog.c2b

import java.nio.file.Path

import com.github.chaabaj.backlog4s.apis._
import com.github.chaabaj.backlog4s.datas._
import com.github.chaabaj.backlog4s.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.exceptions.CybozuLiveImporterException
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL
import com.nulabinc.backlog.c2b.services.MappingFiles
import com.osinka.i18n.Messages

import scala.util.{Failure, Success}

object Validations extends Logger {

  private val backlogName = Messages("name.backlog")
  private val cybozuName = Messages("name.cybozu")
  private val userMappingName = Messages("name.mapping.user")
  private val priorityMappingName = Messages("name.mapping.priority")
  private val statusMappingName = Messages("name.mapping.status")

  implicit class ValidationOps(program: AppProgram[Boolean]) {
    def validate(success: String, failure: String): AppProgram[Unit] =
      program.map { result =>
        if (result)
          AppDSL.fromConsole(ConsoleDSL.print(success))
        else
          throw CybozuLiveImporterException(failure)
      }
  }

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
              throw CybozuLiveImporterException(Messages("message.import.cancel"))
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
        throw CybozuLiveImporterException(Messages("validation.db.exists.error"))
      }
    } yield ()
  }

  def checkMappingFilesExist(config: Config): AppProgram[Unit] =
    for {
      _ <- checkUserMappingFileExists(config.USERS_PATH)
      _ <- checkPriorityMappingFileExists(config.PRIORITIES_PATH)
      _ <- checkStatusMappingFileExists(config.STATUSES_PATH)
    } yield ()

  def checkMappingFilesCSVFormatIfExist(config: Config): AppProgram[Unit] =
    for {
      userExists <- fileExists(config.USERS_PATH)
      _ <- if (userExists) checkMappingFileCSVFormat(config.USERS_PATH, userMappingName) else AppDSL.empty
      priorityExists <- fileExists(config.PRIORITIES_PATH)
      _ <- if (priorityExists) checkMappingFileCSVFormat(config.PRIORITIES_PATH, priorityMappingName) else AppDSL.empty
      statusExists <- fileExists(config.STATUSES_PATH)
      _ <- if (statusExists) checkMappingFileCSVFormat(config.STATUSES_PATH, statusMappingName) else AppDSL.empty
    } yield ()

  private def checkMappingFileCSVFormat(path: Path, mappingFileKind: String): AppProgram[Unit] = {
    for {
      stream <- MappingFiles.read(path)
      result <- AppDSL.streamAsSeq(stream)
      _ <- result match {
        case Success(_) =>
          AppDSL.empty
        case Failure(ex) =>
          val r = """.*?\(startline (\d+?)\) EOF reached.*""".r
          ex.getMessage match {
            case r(line) =>
              throw CybozuLiveImporterException(s"Cannot parse $mappingFileKind mapping file. Line: $line. Path: ${path.toFile.getAbsolutePath}")
            case _ =>
              throw ex
          }
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
      _ <- fileExists(path).validate(
        success = Messages("validation.mapping.file.exists.ok", userMappingName),
        failure = Messages("validation.mapping.file.exists.error", userMappingName)
      )
    } yield ()

  private def checkPriorityMappingFileExists(path: Path): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists")))
      _ <- fileExists(path).validate(
        success = Messages("validation.mapping.file.exists.ok", priorityMappingName),
        failure = Messages("validation.mapping.file.exists.error", priorityMappingName)
      )
    } yield ()


  private def checkStatusMappingFileExists(path: Path): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("validation.mapping.file.exists")))
      _ <- fileExists(path).validate(
        success = Messages("validation.mapping.file.exists.ok", statusMappingName),
        failure = Messages("validation.mapping.file.exists.error", statusMappingName)
      )
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
                  throw CybozuLiveImporterException(Messages("validation.mapping.item.empty", backlogName, userMappingName, cybozuName, cybozu))
                case (cybozu, backlog) =>
                  val userExists = users.exists(_.userId.contains(backlog))
                  if (userExists) {
                    AppDSL.pure(())
                  } else {
                    throw CybozuLiveImporterException(Messages("validation.mapping.item.error", backlogName, userMappingName, cybozuName, cybozu, backlog))
                  }
              }
            )
          } yield ()
        case Left(error) =>
          throw CybozuLiveImporterException("Get backlog users fail. " + error.toString)
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
                  throw CybozuLiveImporterException(Messages("validation.mapping.item.empty", backlogName, priorityMappingName, cybozuName, cybozu))
                case (cybozu, backlog) =>
                  val exists = priorities.exists(_.name == backlog)
                  if (exists) {
                    AppDSL.pure(())
                  } else {
                    throw CybozuLiveImporterException(Messages("validation.mapping.item.error", backlogName, priorityMappingName, cybozuName, cybozu, backlog))
                  }
              }
            )
          } yield ()
        case Left(error) =>
          throw CybozuLiveImporterException("Get backlog priorities fail. " + error.toString)
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
                  throw CybozuLiveImporterException(Messages("validation.mapping.item.empty", backlogName, statusMappingName, cybozuName, cybozu))
                case (cybozu, backlog) =>
                  val exists = statuses.exists(_.name == backlog)
                  if (exists) {
                    AppDSL.pure(())
                  } else {
                    throw CybozuLiveImporterException(Messages("validation.mapping.item.error", backlogName, statusMappingName, cybozuName, cybozu, backlog))
                  }
              }
            )
          } yield ()
        case Left(error) =>
          throw CybozuLiveImporterException("Get backlog statuses fail. " + error.toString)
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

  private def fileExists(path: Path): AppProgram[Boolean] =
    AppDSL.fromStorage(StorageDSL.exists(path))

}
