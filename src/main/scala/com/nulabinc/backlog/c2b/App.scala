package com.nulabinc.backlog.c2b

import java.util.Locale

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.chaabaj.backlog4s.apis.AllApi
import com.github.chaabaj.backlog4s.datas.UserT
import com.github.chaabaj.backlog4s.interpreters.AkkaHttpInterpret
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.core._
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, AppInterpreter, ConsoleDSL, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.interpreters.file.LocalStorageInterpreter
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.SQLiteInterpreter
import com.nulabinc.backlog.c2b.services._
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import com.nulabinc.backlog.migration.common.utils.{DateUtil, TrackingData}
import com.osinka.i18n.Messages
import monix.eval.Task
import monix.execution.Scheduler
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure

object App extends Logger {

  def main(args: Array[String]): Unit = {

    // Check
    (for {
      _ <- DataDirectoryChecker.check(Config.App.dataDirectory)
      _ <- ClassVersionChecker.check()
      _ <- DisableSSLCertificateChecker.check()
    } yield ()) match {
      case Failure(ex) => exit(1, ex)
      case _ => ()
    }

    // check release version

    // Initialize
    AnsiConsole.systemInstall()
    setLanguage(Config.App.language)

    val config = ConfigParser.parse(args) match {
      case Some(c) => c.commandType match {
        case Some(InitCommand) => c
        case Some(ImportCommand) => c
        case None => throw new RuntimeException("No command found")
      }
      case None => sys.exit(1)
    }

    implicit val system: ActorSystem = ActorSystem("main")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

    val interpreter = new AppInterpreter(
      backlogInterpreter = new AkkaHttpInterpret(ProxyConfig.create),
      storageInterpreter = new LocalStorageInterpreter,
      storeInterpreter = new SQLiteInterpreter(Config.DB_PATH),
      consoleInterpreter = new ConsoleInterpreter
    )

    val program = for {
      _ <- printBanner()
      _ <- config.commandType match {
        case Some(InitCommand) => init(config, Config.App.language)
        case Some(ImportCommand) => `import`(config, Config.App.language)
        case None => throw new RuntimeException("Invalid command type")
      }
    } yield ()

    val cleanup = interpreter.terminate().flatMap(_ =>
      Task.fromFuture {
        system.terminate()
      }
    )

    interpreter
      .run(program)
      .flatMap(_ => cleanup)
      .onErrorHandleWith { ex =>
        cleanup.map(_ => exit(1, ex))
      }
      .runAsync
  }

  def init(config: Config, language: String): AppProgram[Unit] = {

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)

    val csvFiles = Config.DATA_PATHS.toFile.listFiles().filter(_.getName.endsWith(".csv"))

    for {
      // Initialize
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(Config.MAPPING_PATHS))
      _ <- AppDSL.fromStorage(StorageDSL.createDirectory(Config.TEMP_PATHS))
      // Validation
      _ <- Validations.checkBacklog(config, backlogApi.spaceApi)
      _ <- Validations.checkMappingFilesCSVFormatIfExist()
      // Delete operations
      _ <- AppDSL.fromStorage(StorageDSL.deleteFile(Config.DB_PATH))
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
      _ <- MappingFileConsole.show()
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.finish")))
    } yield ()
  }

  def `import`(config: Config, language: String): AppProgram[Unit] = {
    import com.github.chaabaj.backlog4s.dsl.syntax._

    val backlogApi = AllApi.accessKey(s"${config.backlogUrl}/api/v2/", config.backlogKey)
    val backlogApiConfiguration = BacklogApiConfiguration(
      url = config.backlogUrl,
      key = config.backlogKey,
      projectKey = config.projectKey,
      backlogOutputPath = Config.BACKLOG_PATHS
    )

    for {
      // Initialize
      _ <- AppDSL.fromStorage(StorageDSL.deleteDirectory(Config.BACKLOG_PATHS))
      // Validation
      _ <- Validations.checkBacklog(config, backlogApi.spaceApi)
      _ <- Validations.checkDBExists(Config.DB_PATH)
      _ <- Validations.checkMappingFilesExist()
      _ <- Validations.checkMappingFilesCSVFormatIfExist()
      _ <- Validations.checkMappingFileItems(backlogApi)
      _ <- Validations.projectExists(config, backlogApi.projectApi)
      // Read mapping files
      mappingContext <- MappingFiles.createMappingContext()
      _ <- BacklogExport.all(config)(mappingContext)
      _ <- AppDSL.`import`(backlogApiConfiguration)
      // MixPanel
      environment <- AppDSL.getBacklogEnvironment(backlogApiConfiguration)
      backlogToolEnvNames = Seq("backlogtool", "us-6")
      token = if (backlogToolEnvNames.contains(environment._2))
        Config.App.Mixpanel.backlogtoolToken
      else
        Config.App.Mixpanel.token
      user <- AppDSL.fromBacklog(backlogApi.userApi.byId(UserT.myself).orFail)
      space <- AppDSL.fromBacklog(backlogApi.spaceApi.current.orFail)
      data = TrackingData(
        product = Config.App.Mixpanel.product,
        spaceId = environment._1,
        envname = environment._2,
        userId = user.id.value,
        srcUrl = "",
        dstUrl = config.backlogUrl,
        srcProjectKey = "",
        dstProjectKey = config.projectKey,
        srcSpaceCreated = "",
        dstSpaceCreated = DateUtil.isoFormat(space.created.toDate))
      _ <- AppDSL.sendTrackingData(token, data)
    } yield ()
  }

  private def printBanner(): AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print(
        s"""
           |${Config.App.title}
           |--------------------------------------------------""".stripMargin
      )
    )

  private def exit(exitCode: Int): Unit = {
    AnsiConsole.systemUninstall()
    sys.exit(exitCode)
  }

  private def exit(exitCode: Int, error: Throwable): Unit = {
    Console.printError("ERROR: " + error.getMessage)
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
