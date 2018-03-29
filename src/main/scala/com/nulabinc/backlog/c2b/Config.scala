package com.nulabinc.backlog.c2b

import java.nio.file.{Path, Paths}

import better.files.File
import com.nulabinc.backlog.c2b.Config._


case class Config(
  backlogUrl: String = "",
  backlogKey: String = "",
  projectKey: String = "",
  commandType: CommandType = Undefined
) {
  val DATA_PATHS: Path = Paths.get("./data")
  val DB_PATH: Path = Paths.get("./data/data.db")
  val USERS_PATH: Path = File("data/users.csv").path
  val PRIORITIES_PATH: Path = File("data/priorities.csv").path
  val STATUSES_PATH: Path = File("data/statuses.csv").path
  val USERS_TEMP_PATH: Path = File("data/users.temp.csv").path
  val PRIORITIES_TEMP_PATH: Path = File("data/priorities.temp.csv").path
  val STATUSES_TEMP_PATH: Path = File("data/statuses.temp.csv").path
}

object Config {

  sealed trait CommandType

  case object Init extends CommandType
  case object Import extends CommandType
  case object Undefined extends CommandType
}



