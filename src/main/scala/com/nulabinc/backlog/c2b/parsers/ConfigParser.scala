package com.nulabinc.backlog.c2b.parsers

import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.Config._

object ConfigParser {

  def parse(args: Array[String], dataDirectory: String): Option[Config] =
    parser
      .parse(args, Config())
      .map(_.copy(dataDirectory = dataDirectory))

  def help(): Unit = {
    parser.parse(Seq("--help"), Config()).getOrElse("")
  }

  private def parser =
    new scopt.OptionParser[Config](Config.App.fileName) {

      head(Config.App.name, Config.App.version)

      opt[String]("backlog.url").required().action( (x, c) =>
        c.copy(backlogUrl = x) ).text("Backlog URL")

      opt[String]("backlog.key").required().action( (x, c) =>
        c.copy(backlogKey = x) ).text("Backlog API key")

      opt[String]("projectKey").required().action( (x, c) =>
        c.copy(projectKey = x) ).text("Backlog Project key")

      cmd("init").action( (_, c) => c.copy(commandType = Some(InitCommand)) ).
        text("Prepare project information")

      cmd("import").action { (_, c) => c.copy(commandType = Some(ImportCommand)) }
        .text("Import project to Backlog.")

      help("help") text "print this usage text."

      override def showUsageOnError = true

    }
}
