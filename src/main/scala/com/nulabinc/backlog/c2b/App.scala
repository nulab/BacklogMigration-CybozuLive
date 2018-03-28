package com.nulabinc.backlog.c2b

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backlog4s.apis.{AllApi, PriorityApi, StatusApi}
import backlog4s.datas.User
import backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.core.{ClassVersionChecker, DisableSSLCertificateChecker, Logger}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.generators.CSVRecordGenerator
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters._
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{Insert, StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.readers.{CybozuCSVReader, ReadResult}
import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import monix.reactive.Observable
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
    import backlog4s.dsl.syntax._

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
      // Validation
      _ <- Validations.backlogProgram(config, backlogApi)
      // Delete database
      _ <- AppDSL.fromStorage(StorageDSL.deleteFile(config.DB_PATH))
      // Create database
      _ <- AppDSL.fromDB(StoreDSL.createDatabase)
      // Read from CSV - Issue
      _ <- readTodoCSVtoStoreDB(todoObservable)
      // Read from CSV - Event
      _ <- readEventCSVtoStoreDB(eventObservable)
      // Read from CSV - Forum
      _ <- readForumCSVtoStoreDB(forumObservable)
      // Collect Backlog priorities
      _ <- getBacklogPrioritiesToStoreDB(backlogApi.priorityApi)
      // Collect Backlog statuses
      _ <- getBacklogStatusesToStoreDB(backlogApi.statusApi)
      // Collect Backlog users
      users <- AppDSL.fromBacklog(backlogApi.userApi.all().orFail)
      _ <- AppDSL.consumeStream(
          streamBacklogUsers(Observable.fromIterator(users.iterator))
        )
      // Write mapping files
      _ <- writeMappingFiles(config)
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

  def sequential[A](prgs: Seq[StoreProgram[A]]): StoreProgram[Seq[A]] =
    prgs.foldLeft(StoreDSL.pure(Seq.empty[A])) {
      case (newPrg, prg) =>
        newPrg.flatMap { results =>
          prg.map { result =>
            results :+ result
          }
        }
    }

  def insertOrUpdateUser(cybozuUser: CybozuUser): StoreProgram[AnyId] =
    for {
      optId <- StoreDSL.getCybozuUserByKey(cybozuUser.userId)
      id <- optId match {
        case Some(existUser) => StoreDSL.pure(existUser.id)
        case None => StoreDSL.storeCybozuUser(cybozuUser, Insert)
      }
    } yield id

  def storeCommentsProgram(issueId: AnyId, csvComments: Seq[CybozuCSVComment]): StoreProgram[Unit] = {
    val commentsPrograms = csvComments.map { comment =>
      val commentCreator = CybozuUser.from(comment.creator)
      for {
        creatorId <- insertOrUpdateUser(commentCreator)
      } yield CybozuComment.from(issueId, comment, creatorId)
    }
    for {
      comments <- sequential(commentsPrograms)
      _ <- StoreDSL.storeTodoComments(comments)
    } yield ()
  }

  def readTodoCSVtoStoreDB(observable: Observable[ReadResult[CybozuCSVTodo]]): AppProgram[Unit] = {

    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuUser.from(result.issue.creator)
          val updater = CybozuUser.from(result.issue.updater)
          val assignees = result.issue.assignees.map(u => CybozuUser.from(u))
          for {
            // Save issues
            creatorId <- insertOrUpdateUser(creator)
            updaterId <- insertOrUpdateUser(updater)
            issueId <- {
              val issue = CybozuTodo.from(
                todo = result.issue,
                creatorId = creatorId,
                updaterId = updaterId
              )
              StoreDSL.storeTodo(issue)
            }
            // Save assignees
            assigneeIdsProgram = assignees.map(insertOrUpdateUser)
            assigneeIds <- sequential(assigneeIdsProgram)
            _ <- StoreDSL.storeTodoAssignees(issueId, assigneeIds)
            // Save comments
            _ <- storeCommentsProgram(issueId, result.comments)
          } yield ()
        }
      )
    } yield ()

    AppDSL.fromDB(dbProgram)
  }

  def readEventCSVtoStoreDB(observable: Observable[ReadResult[CybozuCSVEvent]]): AppProgram[Unit] = {
    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuUser.from(result.issue.creator)
          for {
            // Save event
            creatorId <- insertOrUpdateUser(creator)
            eventId <- {
              val issue = CybozuEvent.from(
                event = result.issue,
                creatorId = creatorId
              )
              StoreDSL.storeEvent(issue)
            }
            // Save comments
            _ <- storeCommentsProgram(eventId, result.comments)
          } yield ()
        }
      )
    } yield ()
    AppDSL.fromDB(dbProgram)
  }

  def readForumCSVtoStoreDB(observable: Observable[ReadResult[CybozuCSVForum]]): AppProgram[Unit] = {
    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuUser.from(result.issue.creator)
          val updater = CybozuUser.from(result.issue.updater)
          for {
            // Save event
            creatorId <- insertOrUpdateUser(creator)
            updaterId <- insertOrUpdateUser(updater)
            forumId <- {
              val forum = CybozuForum.from(
                forum = result.issue,
                creatorId = creatorId,
                updaterId = updaterId
              )
              StoreDSL.storeForum(forum)
            }
            // Save comments
            _ <- storeCommentsProgram(forumId, result.comments)
          } yield ()
        }
      )
    } yield ()
    AppDSL.fromDB(dbProgram)
  }

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

  def streamBacklogUsers(userStream: Observable[User]): Observable[AppProgram[Unit]] =
    userStream.map { user =>
      for {
        _ <- AppDSL.pure(user)
        _ <- AppDSL.fromDB(StoreDSL.storeBacklogUser(BacklogUser.from(user)))
      } yield ()
    }

  def writeMappingFiles(config: Config): AppProgram[Unit] = {

    def writeUserMapping: AppProgram[Unit] =
      for {
        backlogUser <- AppDSL.fromDB(StoreDSL.getBacklogUsers)
        _ <- AppDSL.fromStorage(
          for {
            _ <- StorageDSL.writeNewFile(config.USERS_PATH, CSVRecordGenerator.backlogUserToByteArray(backlogUser))
            _ <- StorageDSL.writeAppendFile(config.USERS_PATH, CSVRecordGenerator.splitToByteArray())
          } yield ()
        )
        cybozuUser <- AppDSL.fromDB(StoreDSL.getCybozuUsers)
        _ <- AppDSL.fromStorage(
          StorageDSL.writeAppendFile(config.USERS_PATH, CSVRecordGenerator.cybozuUserToByteArray(cybozuUser))
        )
      } yield ()

    def writePriorityMapping: AppProgram[Unit] =
      for {
        backlogPriority <- AppDSL.fromDB(StoreDSL.getBacklogPriorities)
        _ <- AppDSL.fromStorage(
          for {
            _ <- StorageDSL.writeNewFile(config.PRIORITIES_PATH, CSVRecordGenerator.backlogPriorityToByteArray(backlogPriority))
            _ <- StorageDSL.writeAppendFile(config.PRIORITIES_PATH, CSVRecordGenerator.splitToByteArray())
          } yield ()
        )
        cybozuPrioritity <- AppDSL.fromDB(StoreDSL.getCybozuPriorities)
        _ <- AppDSL.fromStorage(
          StorageDSL.writeAppendFile(config.PRIORITIES_PATH, CSVRecordGenerator.cybozuPriorityToByteArray(cybozuPrioritity))
        )
      } yield ()

    def writeStatusMapping: AppProgram[Unit] =
      for {
        backlogStatus <- AppDSL.fromDB(StoreDSL.getBacklogStatuses)
        _ <- AppDSL.fromStorage(
          for {
            _ <- StorageDSL.writeNewFile (config.STATUSES_PATH, CSVRecordGenerator.backlogStatusToByteArray(backlogStatus))
            _ <- StorageDSL.writeAppendFile(config.STATUSES_PATH, CSVRecordGenerator.splitToByteArray())
          } yield ()
        )
        cybozuStatus <- AppDSL.fromDB(StoreDSL.getCybozuStatuses)
        _ <- AppDSL.fromStorage(
          StorageDSL.writeAppendFile(config.STATUSES_PATH, CSVRecordGenerator.cybozuStatusToByteArray(cybozuStatus))
        )
      } yield ()

    for {
      _ <- writeUserMapping
      _ <- writePriorityMapping
      _ <- writeStatusMapping
    } yield ()
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
