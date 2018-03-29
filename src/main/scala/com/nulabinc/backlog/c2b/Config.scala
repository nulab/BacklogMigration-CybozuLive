package com.nulabinc.backlog.c2b

import java.nio.charset.Charset
import java.nio.file.{Path, Paths}

import better.files.File
import com.nulabinc.backlog.c2b.Config._
import org.apache.commons.csv.CSVFormat


case class Config(
  backlogUrl: String = "",
  backlogKey: String = "",
  projectKey: String = "",
  commandType: CommandType = Undefined
)

object Config {

  val charset: Charset = Charset.forName("UTF-8")

  val csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()

  val DATA_PATHS: Path = Paths.get("./data")
  val MAPPING_PATHS: Path = Paths.get(DATA_PATHS.toRealPath() + "/mappings")
  val TEMP_PATHS: Path = Paths.get(DATA_PATHS.toRealPath() + "/temp")

  val DB_PATH: Path = Paths.get(DATA_PATHS.toRealPath() + "/data.db")

  lazy val USERS_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/users.csv").path
  lazy val BACKLOG_USER_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/user_list.csv").path
  lazy val USERS_TEMP_PATH: Path = File(TEMP_PATHS.toRealPath() + "/users.temp.csv").path

  lazy val PRIORITIES_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/priorities.csv").path
  lazy val BACKLOG_PRIORITY_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/priority_list.csv").path
  lazy val PRIORITIES_TEMP_PATH: Path = File(TEMP_PATHS.toRealPath() + "/priorities.temp.csv").path

  lazy val STATUSES_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/statuses.csv").path
  lazy val BACKLOG_STATUS_PATH: Path = File(MAPPING_PATHS.toRealPath() + "/status_list.csv").path
  lazy val STATUSES_TEMP_PATH: Path = File(TEMP_PATHS.toRealPath() + "/statuses.temp.csv").path

  sealed trait CommandType

  case object Init extends CommandType
  case object Import extends CommandType
  case object Undefined extends CommandType
}



