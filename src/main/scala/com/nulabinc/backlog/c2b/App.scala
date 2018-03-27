package com.nulabinc.backlog.c2b

import java.nio.file.{Path, Paths}
import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backlog4s.apis.{AllApi, PriorityApi, StatusApi, UserApi}
import backlog4s.datas.User
import backlog4s.interpreters.AkkaHttpInterpret
import backlog4s.streaming.ApiStream
import backlog4s.streaming.ApiStream.ApiStream
import better.files.File
import cats.free.Free
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.converters.CybozuConverter
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.generators.CSVRecordGenerator
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
import monix.reactive.Observable
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
//      _ <- readIssueCSVtoStoreDB(issueObservable)
      // Read from CSV - Event
//      _ <- readEventCSVtoStoreDB(eventObservable)
      // Read from CSV - Forum
//      _ <- readForumCSVtoStoreDB(forumObservable)
      // Collect Backlog priorities
      _ <- getBacklogPrioritiesToStoreDB(backlogApi.priorityApi)
      // Collect Backlog statuses
      _ <- getBacklogStatusesToStoreDB(backlogApi.statusApi)
      // Collect Backlog users
      userStream = ApiStream.sequential(Int.MaxValue) (
        (index, count) => backlogApi.userApi.all(offset = index, limit = count)
      )
      stream <- streamBacklogUsers(userStream)
      // Write mapping files
      _ <- writeMappingFiles()
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

  def readIssueCSVtoStoreDB(observable: Observable[(CybozuIssue, Seq[CybozuCSVComment])]): AppProgram[Unit] =
    for {
      issueId <- AppDSL.fromDB(StoreDSL.writeDBStream(observable.map(issue => StoreDSL.storeIssue(issue._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          observable.map { data =>
            val comments = CybozuConverter.toComments(issueId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
    } yield ()

  def readEventCSVtoStoreDB(observable: Observable[(CybozuEvent, Seq[CybozuCSVComment])]): AppProgram[Unit] =
    for {
      eventId <- AppDSL.fromDB(StoreDSL.writeDBStream(observable.map(event => StoreDSL.storeEvent(event._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          observable.map { data =>
            val comments = CybozuConverter.toComments(eventId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
    } yield ()

  def readForumCSVtoStoreDB(observable: Observable[(CybozuForum, Seq[CybozuCSVComment])]): AppProgram[Unit] =
    for {
      forumId <- AppDSL.fromDB(StoreDSL.writeDBStream(observable.map(forum => StoreDSL.storeForum(forum._1))))
      _ <- AppDSL.fromDB(
        StoreDSL.writeDBStream {
          observable.map { data =>
            val comments = CybozuConverter.toComments(forumId, data._2)
            StoreDSL.storeComments(comments)
          }
        }
      )
    } yield ()

  def getBacklogPrioritiesToStoreDB(api: PriorityApi): AppProgram[Unit] =
    for {
      backlogPriorities <- AppDSL.fromBacklog(api.all)
      _ <- backlogPriorities match {
        case Right(data) =>
          val items = data.map(p => BacklogPriority(0, p.name))
          AppDSL.fromDB(StoreDSL.storeBacklogPriorities(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()

  def getBacklogStatusesToStoreDB(api: StatusApi): AppProgram[Unit] =
    for {
      backlogStatuses <- AppDSL.fromBacklog(api.all)
      _ <- backlogStatuses match {
        case Right(data) =>
          val items = data.map(p => BacklogStatus(0, p.name))
          AppDSL.fromDB(StoreDSL.storeBacklogStatuses(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()

  def streamBacklogUsers(userStream: ApiStream[User]): Free[AppADT, Observable[Seq[Free[AppADT, Unit]]]] =
    for {
      streamUsers <- AppDSL.fromBacklogStream(userStream)
      stream = streamUsers.map { users =>
        users.map { user =>
          for {
            _ <- AppDSL.pure(user)
            _ <- AppDSL.fromDB(StoreDSL.storeBacklogUser(BacklogUser.from(user)))
          } yield ()
        }
      }
    } yield stream

  def writeMappingFiles(): AppProgram[Unit] =
    for {
      user <- AppDSL.fromDB(StoreDSL.getBacklogUsers)
      _ <- AppDSL.fromStorage(StorageDSL.writeFile(File("data/users.json").path, CSVRecordGenerator.userToByteArray(user)))
      priorities <- AppDSL.fromDB(StoreDSL.getBacklogPriorities)
      _ <- AppDSL.fromStorage(StorageDSL.writeFile(File("data/priorities.json").path, CSVRecordGenerator.priorityToByteArray(priorities)))
      statuses <- AppDSL.fromDB(StoreDSL.getBacklogStatuses)
      _ <- AppDSL.fromStorage(StorageDSL.writeFile(File("data/statuses.json").path, CSVRecordGenerator.statusToByteArray(statuses)))
    } yield ()

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
