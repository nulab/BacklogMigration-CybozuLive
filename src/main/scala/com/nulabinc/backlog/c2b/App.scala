package com.nulabinc.backlog.c2b

import java.nio.file.{Path, Paths}
import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backlog4s.apis.AllApi
import backlog4s.interpreters.AkkaHttpInterpret
import backlog4s.streaming.ApiStream
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.converters.CybozuConverter
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.TaskUtils.Suspend
import com.nulabinc.backlog.c2b.interpreters._
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.utils.{ClassVersionChecker, DisableSSLCertificateChecker}
import com.osinka.i18n.Messages
import com.typesafe.config.ConfigFactory
import monix.eval.Task
import monix.execution.Scheduler
import org.apache.commons.csv.CSVFormat
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App extends Logger {

  val DATA_PATHS: Path = Paths.get("./data")
  val DB_PATH: Path = Paths.get("./data/data.db")

  def main(args: Array[String]): Unit = {

    // config
    val configFactory = ConfigFactory.load()
    val appConfig     = configFactory.getConfig("app")
    val appName       = appConfig.getString("name")
    val appVersion    = appConfig.getString("version")
    val language      = appConfig.getString("language")

    // start
    Console.printBanner(appName, appVersion)

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
    val forumFiles = csvFiles.filter(_.getName.contains("live_掲示板_"))

    val issueObservable = CybozuConverter.toIssue(todoFiles, csvFormat)
    val eventObservable = CybozuConverter.toEvent(eventFiles, csvFormat)
    val forumObservable = CybozuConverter.toForum(forumFiles, csvFormat)

    val program = for {
      // Validation
      _ <- validationProgram(config, backlogApi)
      // Delete database
      _ <- AppDSL.fromStorage(StorageDSL.deleteFile(DB_PATH))
      // Create database
      _ <- AppDSL.fromDB(StoreDSL.createDatabase)
      // Read from CSV - Issue
      issueId <- AppDSL.fromDB(StoreDSL.writeDBStream(issueObservable.map(issue => StoreDSL.storeIssue(issue._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          issueObservable.map { data =>
            val comments = CybozuConverter.toComments(issueId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
      // Read from CSV - Event
//      eventId <- AppDSL.fromDB(StoreDSL.writeDBStream(eventObservable.map(event => StoreDSL.storeEvent(event._1))))
//      _ <- AppDSL.fromDB(
//        StoreDSL.writeDBStream {
//          eventObservable.map { data =>
//            val comments = CybozuConverter.toComments(eventId, data._2)
//            StoreDSL.storeComments(comments)
//          }
//        }
//      )
      // Read from CSV - Forum
//      forumId <- AppDSL.fromDB(StoreDSL.writeDBStream(forumObservable.map(forum => StoreDSL.storeForum(forum._1))))
//      _ <- AppDSL.fromDB(
//        StoreDSL.writeDBStream {
//          eventObservable.map { data =>
//            val comments = CybozuConverter.toComments(forumId, data._2)
//            StoreDSL.storeComments(comments)
//          }
//        }
//      )
      // Collect Backlog users
      userStream = ApiStream.sequential(Int.MaxValue) (
        (index, count) => backlogApi.userApi.all(offset = index, limit = count)
      )
      streamUsers <- AppDSL.fromBacklogStream(userStream)
      stream = streamUsers.map { users =>
        users.map { user =>
          for {
            _ <- AppDSL.pure(user)
            _ <- AppDSL.fromConsole(ConsoleDSL.print(user.toString))
          } yield ()
        }
      }
    } yield stream

    val f = interpreter.run(program).flatMap { userStream =>
      val future = userStream.mapTask { prgs =>
        TaskUtils.sequential(
          prgs.map { prg =>
            Suspend(() => interpreter.run(prg))
          }
        )
      }
      Task.deferFuture {
        future.runAsyncGetFirst
      }
    }.runAsync


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
