package com.nulabinc.backlog.c2b

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backlog4s.apis.AllApi
import backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.core.{ClassVersionChecker, DisableSSLCertificateChecker, Logger}
import com.nulabinc.backlog.c2b.interpreters._
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.readers.CybozuCSVReader
import com.nulabinc.backlog.c2b.services.{BacklogToStore, CSVtoStore, MappingFiles}
import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import org.apache.commons.csv.CSVFormat
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App extends Logger {

  def main(args: Array[String]): Unit = {

    // config
    val configFactory = ConfigFactory.load()
    val appConfig = configFactory.getConfig("app")
    val appName = appConfig.getString("name")
    val appVersion = appConfig.getString("version")
    val language = appConfig.getString("language")

    // start
    Console.printBanner(appName, appVersion)

    // ------------------------------------------------------------------------
    // check
    // ------------------------------------------------------------------------
    ClassVersionChecker.check() match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }
    DisableSSLCertificateChecker.check() match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }
    // TODO: check release version

    ConfigParser(appName, appVersion).parse(args) match {
      case Some(config) => config.commandType match {
        case Init => init(config, language)
        case Import => `import`(config, language)
        case _ => throw new RuntimeException("It never happens")
      }
      case None => throw new RuntimeException("It never happens")
    }
  }

  def init(config: Config, language: String): Unit = {

    implicit val system: ActorSystem = ActorSystem("init")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

    val interpreter = new AppInterpreter(
      backlogInterpreter = new AkkaHttpInterpret,
      storageInterpreter = new LocalStorageInterpreter,
      dbInterpreter = new SQLiteInterpreter("db.main"),
      consoleInterpreter = new ConsoleInterpreter
    ) // TODO: proxy

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()
    val csvFiles = config.DATA_PATHS.toFile.listFiles().filter(_.getName.endsWith(".csv"))
    val todoFiles = {
      csvFiles.filter(_.getName.contains("live_ToDo")) ++
      csvFiles.filter(_.getName.contains("live_To-Do List"))
    }
    val eventFiles = {
      csvFiles.filter(_.getName.contains("live_Events_")) ++
      csvFiles.filter(_.getName.contains("live_イベント_"))
    }
    val forumFiles = csvFiles.filter(_.getName.contains("live_掲示板_"))

    val todoObservable = CybozuCSVReader.toCybozuTodo(todoFiles, csvFormat)
    val eventObservable = CybozuCSVReader.toCybozuEvent(eventFiles, csvFormat)
    val forumObservable = CybozuCSVReader.toCybozuForum(forumFiles, csvFormat)

    val program = for {
      // Initialize
      _ <- AppDSL.pure(AnsiConsole.systemInstall())
      _ <- AppDSL.setLanguage(language)
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(config.MAPPING_PATHS))
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
    } yield ()

    val f = interpreter.run(program).runAsync

    Await.result(f, Duration.Inf)

    system.terminate()
  }

  def `import`(config: Config, language: String): Unit = {

    implicit val system: ActorSystem = ActorSystem("import")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

    val interpreter = new AppInterpreter(
      backlogInterpreter = new AkkaHttpInterpret,
      storageInterpreter = new LocalStorageInterpreter,
      dbInterpreter = new SQLiteInterpreter("db.main"),
      consoleInterpreter = new ConsoleInterpreter
    )

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val program = for {
      // Initialize
      _ <- AppDSL.pure(AnsiConsole.systemInstall())
      _ <- AppDSL.setLanguage(language)
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi)
      _ <- Validations.dbExistsProgram(config.DB_PATH)
      _ <- Validations.mappingFilesExistProgram(config)
    } yield ()

    val f = interpreter.run(program).runAsync

    Await.result(f, Duration.Inf)

    system.terminate()
  }

  private def exit(exitCode: Int): Unit = {
    AnsiConsole.systemUninstall()
    sys.exit(exitCode)
  }

  private def exit(exitCode: Int, error: Throwable): Unit = {
    Console.printError(error)
    exit(exitCode)
  }

}
