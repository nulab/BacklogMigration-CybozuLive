package com.nulabinc.backlog.c2b

import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.chaabaj.backlog4s.apis.AllApi
import com.github.chaabaj.backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.core.{ClassVersionChecker, DataDirectoryChecker, DisableSSLCertificateChecker, Logger}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, AppInterpreter, ConsoleDSL, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.services._
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import com.osinka.i18n.Messages
import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure

object App extends Logger {

  def main(args: Array[String]): Unit = {

    // Config
    val configFactory = ConfigFactory.load()
    val appConfig = configFactory.getConfig("app")
    val appName = appConfig.getString("name")
    val appVersion = appConfig.getString("version")
    val language = appConfig.getString("language")
    val dataDirectory = appConfig.getString("dataDirectory")

    // Check
    (for {
      _ <- DataDirectoryChecker.check(dataDirectory)
      _ <- ClassVersionChecker.check()
      _ <- DisableSSLCertificateChecker.check()
    } yield ()) match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }

    // check release version

    // Initialize
    AnsiConsole.systemInstall()
    setLanguage(language)

    val config = ConfigParser(appName, appVersion).parse(args, dataDirectory) match {
      case Some(c) => c.commandType match {
        case Some(InitCommand) => c
        case Some(ImportCommand) => c
        case None => throw new RuntimeException("No command found")
      }
      case None => throw new RuntimeException("Invalid configuration")
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
        case Some(InitCommand) => init(config, language)
        case Some(ImportCommand) => `import`(config, language)
        case None => AppDSL.exit("Invalid command type", 1)
      }
    } yield ()

    interpreter
      .run(program)
      .flatMap(_ => interpreter.terminate())
      .runAsync
      .flatMap(_ => system.terminate())
      .onComplete(_ => exit(0))
  }

  def init(config: Config, language: String): AppProgram[Unit] = {

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val csvFiles = config.DATA_PATHS.toFile.listFiles().filter(_.getName.endsWith(".csv"))

    for {
      // Initialize
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(config.MAPPING_PATHS))
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(config.TEMP_PATHS))
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi.spaceApi)
      // Delete operations
      _ <- AppDSL.fromStorage(StorageDSL.deleteFile(config.DB_PATH))
      _ <- AppDSL.fromStore(StoreDSL.createDatabase)
      // Read CSV and to store
      _ <- CybozuStore.copyToStore(csvFiles)
      // Collect Backlog data to store
      _ <- BacklogService.storePriorities(backlogApi.priorityApi)
      _ <- BacklogService.storeStatuses(backlogApi.statusApi)
      _ <- BacklogService.storeUsers(backlogApi.userApi)
      // Write mapping files
      _ <- MappingFiles.write(config)
      // Finalize
      _ <- MappingFileConsole.show(config)
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
      _ <- AppDSL.fromStorage(StorageDSL.deleteDirectory(config.BACKLOG_PATHS))
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi.spaceApi)
      _ <- Validations.dbExistsProgram(config.DB_PATH)
      _ <- Validations.mappingFilesExistProgram(config)
      _ <- Validations.mappingFileItems(config, backlogApi)
      _ <- Validations.projectsExists(config, backlogApi.projectApi)
      // Read mapping files
      mappingContext <- MappingFiles.createMappingContext(config)
      _ <- BacklogExport.all(config)(mappingContext)
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

  private def setLanguage(locale: String): Unit =
    locale match {
      case "ja" => Locale.setDefault(Locale.JAPAN)
      case "en" => Locale.setDefault(Locale.US)
      case _ => ()
    }

}
