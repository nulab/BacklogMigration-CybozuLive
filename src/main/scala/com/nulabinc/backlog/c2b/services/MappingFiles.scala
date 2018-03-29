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
import org.apache.commons.csv.{CSVFormat, CSVParser, CSVRecord}

import scala.collection.immutable.HashMap

object MappingFiles {

  import CSVRecordSerializer._

  def write(config: Config): AppProgram[Unit] =
    for {
      _ <- writeUserMapping(config)
      _ <- writePriorityMapping(config)
      _ <- writeStatusMapping(config)
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

  private def writeUserMapping(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.CYBOZU_USERS_PATH, config.CYBOZU_USERS_TEMP_PATH)
      )
      cybozuUsersStream <- AppDSL.fromDB(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(cybozuUsersStream)
      newCybozuUsersMap = cybozuUsers.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuUser) =>
          acc + (cybozuUser.userId -> "")
      }
      oldCybozuUserMap <- getOldRecords(config.CYBOZU_USERS_TEMP_PATH)
      mergedCybozuUserMap = DiffPatch.applyChanges(oldCybozuUserMap, newCybozuUsersMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          config.CYBOZU_USERS_PATH,
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(mergedCybozuUserMap).iterator
          )
        )
      )
      backlogUserStream <- AppDSL.fromDB(StoreDSL.getBacklogUsers)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(config.USERS_PATH,
          Observable(CSVRecordSerializer.header) ++
            backlogUserStream.map(user => CSVRecordSerializer.serialize(user))
        )
      )
    } yield ()
  }

  private def writePriorityMapping(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(StorageDSL.copy(config.PRIORITIES_PATH, config.PRIORITIES_TEMP_PATH))
      backlogPriorities <- AppDSL.fromDB(StoreDSL.getBacklogPriorities)
      cybozuPriorities <- AppDSL.fromDB(StoreDSL.getCybozuPriorities)
      backlogPriorityStream = backlogPriorities.map { backlogPriority =>
        CSVRecordSerializer.serialize(backlogPriority)
      }
      cybozuPriorityStream = cybozuPriorities.map { cybozuPriority =>
        CSVRecordSerializer.serialize(cybozuPriority)
      }
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(config.PRIORITIES_PATH,
          Observable(CSVRecordSerializer.header) ++
            backlogPriorityStream ++
            Observable(CSVRecordSerializer.split) ++
            cybozuPriorityStream
        )
      )
    } yield ()
  }

  private def writeStatusMapping(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(StorageDSL.copy(config.STATUSES_PATH, config.STATUSES_TEMP_PATH))
      backlogStatuses <- AppDSL.fromDB(StoreDSL.getBacklogStatuses)
      cybozuStatuses <- AppDSL.fromDB(StoreDSL.getCybozuStatuses)
      backlogStatusStream = backlogStatuses.map { backlogStatus =>
        CSVRecordSerializer.serialize(backlogStatus)
      }
      cybozuStatusStream = cybozuStatuses.map { cybozuStatus =>
        CSVRecordSerializer.serialize(cybozuStatus)
      }
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(config.STATUSES_PATH,
          Observable(CSVRecordSerializer.header) ++
            backlogStatusStream ++
            Observable(CSVRecordSerializer.split) ++
            cybozuStatusStream
        )
      )
    } yield ()
  }


}
