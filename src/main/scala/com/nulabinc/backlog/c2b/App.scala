package com.nulabinc.backlog.c2b

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.chaabaj.backlog4s.apis.AllApi
import com.github.chaabaj.backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.core.{ClassVersionChecker, DisableSSLCertificateChecker, Logger}
import com.nulabinc.backlog.c2b.datas.{CybozuIssueType, IssueType}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, AppInterpreter, ConsoleDSL, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.readers.CybozuCSVReader
import com.nulabinc.backlog.c2b.services._
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import com.osinka.i18n.Messages
import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure
import scala.concurrent.duration.Duration

object App extends Logger {

  def main(args: Array[String]): Unit = {

    // config
    val configFactory = ConfigFactory.load()
    val appConfig = configFactory.getConfig("app")
    val appName = appConfig.getString("name")
    val appVersion = appConfig.getString("version")
    val language = appConfig.getString("language")
    val dataDirectory = appConfig.getString("dataDirectory")

    // ------------------------------------------------------------------------
    // check
    // ------------------------------------------------------------------------
    try {
      Paths.get(dataDirectory).toRealPath()
    } catch {
      case _: Throwable =>
        exit(1, Messages("error.data_folder_not_found", dataDirectory))
    }
    ClassVersionChecker.check() match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }
    DisableSSLCertificateChecker.check() match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }
    // TODO: check release version

    val config = ConfigParser(appName, appVersion).parse(args, dataDirectory) match {
      case Some(c) => c.commandType match {
        case InitCommand => c
        case ImportCommand => c
        case _ => throw new RuntimeException("It never happens")
      }
      case None => throw new RuntimeException("It never happens")
    }

    implicit val system: ActorSystem = ActorSystem("main")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

    val interpreter = new AppInterpreter(
      backlogInterpreter = new AkkaHttpInterpret,
      storageInterpreter = new LocalStorageInterpreter,
      storeInterpreter = new SQLiteInterpreter(config.DB_PATH),
      consoleInterpreter = new ConsoleInterpreter
    )

    val program = for {
      _ <- printBanner(appName, appVersion)
      _ <- config.commandType match {
        case InitCommand => init(config, language)
        case ImportCommand => `import`(config, language)
        case _ => AppDSL.exit("Invalid command type. It never happens", 1)
      }
    } yield ()

    interpreter.run(program).runSyncUnsafe(Duration.Inf)

    system.terminate()
    exit(0)
  }

  def init(config: Config, language: String): AppProgram[Unit] = {

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val csvFiles = config.DATA_PATHS.toFile.listFiles().filter(_.getName.endsWith(".csv"))
    val todoFiles = {
      csvFiles.filter(_.getName.contains("live_ToDo")) ++
      csvFiles.filter(_.getName.contains("live_To-Do List"))
    }
    val eventFiles = {
      csvFiles.filter(_.getName.contains("live_Events_")) ++
      csvFiles.filter(_.getName.contains("live_イベント_"))
    }
    val forumFiles = csvFiles.filter(_.getName.contains("live_掲示板_")) // TODO: english version

    val todoObservable = CybozuCSVReader.toCybozuTodo(todoFiles)
    val eventObservable = CybozuCSVReader.toCybozuEvent(eventFiles)
    val forumObservable = CybozuCSVReader.toCybozuForum(forumFiles)

    for {
      // Initialize
      _ <- AppDSL.pure(AnsiConsole.systemInstall())
      _ <- AppDSL.setLanguage(language)
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(config.MAPPING_PATHS))
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(config.TEMP_PATHS))
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi)
      // Delete operations
      _ <- AppDSL.fromStorage(StorageDSL.deleteFile(config.DB_PATH))
      _ <- AppDSL.fromDB(StoreDSL.createDatabase)
      // Read CSV and to store
      _ <- CSVtoStore.todo(todoObservable)
      _ <- CSVtoStore.event(eventObservable)
      _ <- CSVtoStore.forum(forumObservable)
      // Collect Backlog data to store
      _ <- BacklogToStore.priority(backlogApi.priorityApi)
      _ <- BacklogToStore.status(backlogApi.statusApi)
      _ <- BacklogToStore.user(backlogApi.userApi)
      // Write mapping files
      _ <- MappingFiles.write(config)
      // Finalize
      _ <- MappingFileConsole.to(config)
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.finish")))
    } yield ()
  }

  def `import`(config: Config, language: String): AppProgram[Unit] = {

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)
    val backlogApiConfiguration = BacklogApiConfiguration(
      url = config.backlogUrl,
      key = config.backlogKey,
      projectKey = config.projectKey,
      backlogOutputPath = config.BACKLOG_PATHS
    )

    for {
      // Initialize
      _ <- AppDSL.pure(AnsiConsole.systemInstall())
      _ <- AppDSL.setLanguage(language)
      _ <- AppDSL.fromStorage(StorageDSL.deleteDirectory(config.BACKLOG_PATHS))
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi)
      _ <- Validations.dbExistsProgram(config.DB_PATH)
      _ <- Validations.mappingFilesExistProgram(config)
      _ <- Validations.mappingFileItems(backlogApi, config)
      // Read mapping files
      mappingContext <- MappingFiles.createMappingContext(config)
      _ <- BacklogExport.all(config, issueTypes)(mappingContext)
      _ <- AppDSL.`import`(backlogApiConfiguration)
    } yield ()
  }

  private def printBanner(applicationName: String, applicationVersion: String): AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print(
        s"""
           |$applicationName $applicationVersion
           |--------------------------------------------------""".stripMargin
      )
    )

  private def exit(exitCode: Int): Unit = {
    AnsiConsole.systemUninstall()
    sys.exit(exitCode)
  }

  private def exit(exitCode: Int, error: Throwable): Unit = {
    Console.printError(error)
    exit(exitCode)
  }

  private def exit(exitCode: Int, error: String): Unit = {
    Console.println(error)
    exit(exitCode)
  }

  private def issueTypes: Map[IssueType, CybozuIssueType] =
    Map(
      IssueType.ToDo -> CybozuIssueType(Messages("issue.type.todo")),
      IssueType.Event -> CybozuIssueType(Messages("issue.type.event")),
      IssueType.Forum -> CybozuIssueType(Messages("issue.type.forum"))
    )

}
