package com.nulabinc.backlog.c2b

import java.io.FileWriter
import java.util.Locale

import better.files.File
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.c2b.generators.CSVRecordGenerator
import com.nulabinc.backlog.c2b.parsers.ConfigParser
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.utils.{ClassVersionChecker, DisableSSLCertificateChecker}
import com.typesafe.config.ConfigFactory
import org.apache.commons.csv.{CSVFormat, CSVPrinter}
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure

object App {

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

    import com.nulabinc.backlog.c2b.interpreters.AppDSL._

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
