package com.nulabinc.backlog.c2b

import java.util.Locale

import com.nulabinc.backlog.c2b.cli.ConfigParser
import com.nulabinc.backlog.c2b.core.domain.Config
import com.nulabinc.backlog.c2b.utils.{ClassVersionChecker, DisableSSLCertificateChecker}
import com.typesafe.config.ConfigFactory
import org.fusesource.jansi.AnsiConsole

import scala.util.Failure

object App {

  def main(args: Array[String]): Unit = {

    import com.nulabinc.backlog.c2b.core.domain.Config._

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
    if(!ClassVersionChecker.check()) {
      Console.printClassVersionError()
      sys.exit(1)
    }
    DisableSSLCertificateChecker.check() match {
      case Failure(ex) =>
        Console.error(ex.getMessage)
        sys.exit(1)
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

    // clean up
    AnsiConsole.systemUninstall()

    result match {
      case Success => sys.exit(0)
      case ConfigError => sys.exit(1)
      case Error(ex) =>
        Console.showError(ex)
        sys.exit(1)
    }
  }

  def init(config: Config): AppResult = ???

  def `import`(config: Config): AppResult = ???

  private def setLanguage(language: String): Unit = language match {
    case "ja" => Locale.setDefault(Locale.JAPAN)
    case "en" => Locale.setDefault(Locale.US)
    case _    => ()
  }

}
