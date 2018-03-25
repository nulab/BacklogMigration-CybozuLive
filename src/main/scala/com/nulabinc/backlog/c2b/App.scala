package com.nulabinc.backlog.c2b

import java.nio.charset.Charset
import java.nio.file.{Path, Paths}
import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backlog4s.apis.AllApi
import backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.converters.CybozuConverter
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, AppInterpreter, ConsoleDSL, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.parsers.{CSVRecordParser, ConfigParser}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.utils.{ClassVersionChecker, DisableSSLCertificateChecker}
import com.osinka.i18n.Messages
import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App extends Logger {

  val DATA_PATHS: Path = Paths.get("./data")

  def main(args: Array[String]): Unit = {

    // config
    val configFactory = ConfigFactory.load()
    val appConfig     = configFactory.getConfig("app")
    val appName       = appConfig.getString("name")
    val appVersion    = appConfig.getString("version")
    val language      = appConfig.getString("language")

    // start
    Console.printBanner(appName)

    // ------------------------------------------------------------------------
    // initialize
    // ------------------------------------------------------------------------
    AnsiConsole.systemInstall()
    setLanguage(language)

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

    val result = ConfigParser(appName, appVersion).parse(args) match {
      case Some(config) => config.commandType match {
        case Init => init(config)
        case Import => `import`(config)
        case _ => ConfigError
      }
      case None => ConfigError
    }

    result match {
      case Success     => exit(0)
      case ConfigError => exit(1)
      case Error(ex)   => exit(1, ex)
    }
  }

  def init(config: Config): AppResult = {

    implicit val system: ActorSystem = ActorSystem("init")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

    val interpreter = new AppInterpreter(
      backlogInterpreter = new AkkaHttpInterpret,
      storageInterpreter = new LocalStorageInterpreter,
      dbInterpreter = new SQLiteInterpreter("db.main"),
      consoleInterpreter = new ConsoleInterpreter
    )      // TODO: proxy

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()
    val csvFiles = DATA_PATHS.toFile.listFiles().filter(_.getName.endsWith(".csv"))
    val todoFiles = csvFiles.filter(_.getName.contains("live_To-Do List"))
    val eventFiles = csvFiles.filter(_.getName.contains("live_Events_"))

    val issueObservable = CybozuConverter.toIssue(todoFiles, csvFormat)
    val eventObservable = CybozuConverter.toEvent(eventFiles, csvFormat)

    val program = for {
      _ <- validationProgram(config, backlogApi)
      issueId <- AppDSL.fromDB(StoreDSL.writeDBStream(issueObservable.map(issue => StoreDSL.storeIssue(issue._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          issueObservable.map { data =>
            val comments = CybozuConverter.toComments(issueId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
      eventId <- AppDSL.fromDB(StoreDSL.writeDBStream(eventObservable.map(event => StoreDSL.storeEvent(event._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          eventObservable.map { data =>
            val comments = CybozuConverter.toComments(eventId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
    } yield ()

    val f = interpreter.run(program).runAsync

    Await.result(f, Duration.Inf)

    system.terminate()

//    val writer = new FileWriter("mapping/users.json")
//    val printer = new CSVPrinter(writer, CSVFormat.DEFAULT)

//    val mappingFileProgram = for {
//      user <- fromDB(StoreDSL.getUsers)
////      _ <- fromStorage(StorageDSL.writeFile(File("mapping/users.json").path, CSVRecordGenerator.to(user)))
//      _ <- pure(user.map(u => printer.printRecord(u.key, "")))
//    } yield ()

    Success
  }

  def `import`(config: Config): AppResult = ???

  def validationProgram(config: Config, backlogApi: AllApi): AppProgram[Unit] = {

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

  private def setLanguage(language: String): Unit = language match {
    case "ja" => Locale.setDefault(Locale.JAPAN)
    case "en" => Locale.setDefault(Locale.US)
    case _    => ()
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
