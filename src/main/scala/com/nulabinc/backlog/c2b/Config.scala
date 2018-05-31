package com.nulabinc.backlog.c2b

import java.nio.charset.Charset
import java.nio.file.{Path, Paths}

import better.files.File
import com.nulabinc.backlog.c2b.Config._
import com.nulabinc.backlog.migration.common.conf.BacklogPaths
import com.typesafe.config.ConfigFactory
import org.apache.commons.csv.CSVFormat

case class Config(
  backlogUrl: String = "",
  backlogKey: String = "",
  projectKey: String = "",
  commandType: Option[CommandType] = None
) {
  lazy val backlogPaths = new BacklogPaths(projectKey, BACKLOG_PATHS)
}

object Config {

  val exportedCsvCharset: Charset = Charset.forName("UTF-8")
  val mappingFileCharset: Charset = Charset.defaultCharset() // Mapping files are read by different OSs

  val csvFormat: CSVFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()

  val issueTypes = Seq("ToDo", "Event", "Forum")

  private val config = ConfigFactory.load()

  object App {
    private val applicationConfig = config.getConfig("application")

    val name: String = applicationConfig.getString("name")
    val version: String = applicationConfig.getString("version")
    val title: String = applicationConfig.getString("title")
    val fileName: String = applicationConfig.getString("fileName")
    val language: String = applicationConfig.getString("language")
    val dataDirectory: String = applicationConfig.getString("dataDirectory")

    object Mixpanel {
      private val mixpanelConfig = applicationConfig.getConfig("mixpanel")

      val token: String = mixpanelConfig.getString("token")
      val backlogtoolToken: String = mixpanelConfig.getString("backlogtoolToken")
      val product: String = mixpanelConfig.getString("product")
    }
  }

  lazy val DATA_PATHS: Path = Paths.get(App.dataDirectory)
  lazy val MAPPING_PATHS: Path = Paths.get(DATA_PATHS.toRealPath() + "/mappings")
  lazy val TEMP_PATHS: Path = Paths.get(DATA_PATHS.toRealPath() + "/temp")
  lazy val BACKLOG_PATHS: Path = Paths.get(DATA_PATHS.toRealPath() + "/backlog")

  lazy val DB_PATH: Path = Paths.get(DATA_PATHS.toRealPath() + "/data.db")

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

  case object InitCommand extends CommandType
  case object ImportCommand extends CommandType
}



