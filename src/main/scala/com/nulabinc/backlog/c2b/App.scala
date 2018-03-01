package com.nulabinc.backlog.c2b

import com.nulabinc.backlog.c2b.cli.ConfigParser
import com.nulabinc.backlog.c2b.core.domain.Config

object App {

  def main(args: Array[String]): Unit = {

    import com.nulabinc.backlog.c2b.core.domain.CommandType._

    val result = ConfigParser.parse(args) match {
      case Some(config) => config.commandType match {
        case Init => init(config)
        case Import => `import`(config)
        case _ => Left(throw new RuntimeException("Invalid command type"))
      }
      case None => Left(throw new RuntimeException("Command parsing failed"))
    }

    result match {
      case Right(_) => sys.exit(0)
      case Left(ex) => sys.exit(1)
    }
  }

  def init(config: Config): Either[Throwable, Unit] = ???

  def `import`(config: Config): Either[Throwable, Unit] = ???

}
