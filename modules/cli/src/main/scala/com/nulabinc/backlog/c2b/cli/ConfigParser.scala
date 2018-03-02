package com.nulabinc.backlog.c2b.cli

import com.nulabinc.backlog.c2b.cli.ConfigParser.parser
import com.nulabinc.backlog.c2b.core.domain.Config

case class ConfigParser(applicationName: String, applicationVersion: String) {
  def parse(args: Array[String]): Option[Config] =
    parser(applicationName, applicationVersion).parse(args, Config())

  def help(): Unit = {
    parser(applicationName, applicationVersion).parse(Seq("--help"), Config()).getOrElse("")
  }
}

object ConfigParser {

  import com.nulabinc.backlog.c2b.core.domain.Config._

  private def parser(applicationName: String, applicationVersion: String) =
    new scopt.OptionParser[Config](s"$applicationName-$applicationVersion.jar") {

      head(applicationName, applicationVersion)

      opt[String]("dstBacklogUrl").required().action( (x, c) =>
        c.copy(backlogUrl = x) ).text("Backlog URL")

      opt[String]("dstBacklogKey").required().action( (x, c) =>
        c.copy(backlogKey = x) ).text("Backlog API key")

      opt[String]("projectKey").required().action( (x, c) =>
        c.copy(projectKey = x) ).text("Backlog Project key")

      cmd("init").action( (_, c) => c.copy(commandType = Init) ).
        text("migrate users.")

      cmd("import").action { (_, c) => c.copy(commandType = Import) }
        .text("migrate project.")

      help("help") text "print this usage text."

    }
}
