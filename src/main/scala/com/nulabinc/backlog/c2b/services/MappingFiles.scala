package com.nulabinc.backlog.c2b.services

import java.io.InputStream
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.converters.CSVRecordSerializer
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import monix.reactive.Observable
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.immutable.HashMap

object MappingFiles {

  import CSVRecordSerializer._

  def write: AppProgram[Unit] =
    for {
      _ <- writeUserMapping
      _ <- writePriorityMapping
      _ <- writeStatusMapping
    } yield ()

  private def readCSVFile(is: InputStream): HashMap[String, String] = {
    val parser = CSVParser.parse(is, Config.charset, CSVFormat.DEFAULT)
    parser.getRecords.asScala.foldLeft(HashMap.empty[String, String]) {
      case (acc, record) =>
        acc + (record.get(0) -> record.get(1))
    }
  }

  private def getOldRecords(path: Path): AppProgram[HashMap[String, String]] =
    for {
      oldExists <- AppDSL.fromStorage(
        StorageDSL.exists(path)
      )
      oldRecords <- if (oldExists) {
        AppDSL.fromStorage(
          StorageDSL.readFile(path, readCSVFile)
        )
      } else {
        AppDSL.pure(HashMap.empty[String, String])
      }
    } yield oldRecords

  private def writeUserMapping: AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.USERS_PATH, Config.USERS_TEMP_PATH)
      )
      cybozuUsersStream <- AppDSL.fromDB(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(cybozuUsersStream)
      newCybozuUsersMap = cybozuUsers.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuUser) =>
          acc + (cybozuUser.userId -> "")
      }
      oldCybozuUserMap <- getOldRecords(Config.USERS_TEMP_PATH)
      mergedCybozuUserMap = DiffPatch.applyChanges(oldCybozuUserMap, newCybozuUsersMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          Config.USERS_PATH,
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(mergedCybozuUserMap).iterator
          )
        )
      )
      backlogUserStream <- AppDSL.fromDB(StoreDSL.getBacklogUsers)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(Config.BACKLOG_USER_PATH,
          Observable(CSVRecordSerializer.backlogHeader("User")) ++
          backlogUserStream.map(user => CSVRecordSerializer.serialize(user))
        )
      )
    } yield ()
  }

  private def writePriorityMapping: AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.PRIORITIES_PATH, Config.PRIORITIES_TEMP_PATH)
      )
      cybozuPrioritiesStream <- AppDSL.fromDB(StoreDSL.getCybozuPriorities)
      cybozuPriorities <- AppDSL.streamAsSeq(cybozuPrioritiesStream)
      newCybozuPrioritiesMap = cybozuPriorities.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuPriority) =>
          acc + (cybozuPriority.value -> "")
      }
      oldCybozuPriorityMap <- getOldRecords(Config.PRIORITIES_TEMP_PATH)
      mergedCybozuPriorityMap = DiffPatch.applyChanges(oldCybozuPriorityMap, newCybozuPrioritiesMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          Config.PRIORITIES_PATH,
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(mergedCybozuPriorityMap).iterator
          )
        )
      )
      backlogPriorityStream <- AppDSL.fromDB(StoreDSL.getBacklogPriorities)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(Config.BACKLOG_PRIORITY_PATH,
          Observable(CSVRecordSerializer.backlogHeader("Priority")) ++
          backlogPriorityStream.map(priority => CSVRecordSerializer.serialize(priority))
        )
      )
    } yield ()

  private def writeStatusMapping: AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.STATUSES_PATH, Config.STATUSES_TEMP_PATH)
      )
      cybozuStatusesStream <- AppDSL.fromDB(StoreDSL.getCybozuStatuses)
      cybozuStatuses <- AppDSL.streamAsSeq(cybozuStatusesStream)
      newCybozuStatusesMap = cybozuStatuses.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuStatus) =>
          acc + (cybozuStatus.value -> "")
      }
      oldCybozuStatusesMap <- getOldRecords(Config.STATUSES_TEMP_PATH)
      mergedCybozuStatusMap = DiffPatch.applyChanges(oldCybozuStatusesMap, newCybozuStatusesMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          Config.STATUSES_PATH,
          Observable(CSVRecordSerializer.header) ++
            Observable.fromIterator(
              CSVRecordSerializer.serializeMap(mergedCybozuStatusMap).iterator
            )
        )
      )
      backlogStatusStream <- AppDSL.fromDB(StoreDSL.getBacklogStatuses)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(Config.BACKLOG_STATUS_PATH,
          Observable(CSVRecordSerializer.backlogHeader("Status")) ++
            backlogStatusStream.map(status => CSVRecordSerializer.serialize(status))
        )
      )
    } yield ()


}
