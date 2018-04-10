package com.nulabinc.backlog.c2b.services

import java.io.InputStream
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.converters.CSVRecordSerializer
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.osinka.i18n.Messages
import monix.reactive.Observable
import org.apache.commons.csv.CSVParser

import scala.collection.immutable.HashMap

object MappingFiles {

  import CSVRecordSerializer._

  def write(config: Config): AppProgram[Unit] =
    for {
      _ <- writeUserMapping(config)
      _ <- writePriorityMapping(config)
      _ <- writeStatusMapping(config)
    } yield ()

  def read(path: Path): AppProgram[Observable[(String, String)]] =
    AppDSL.pure(
      Observable.fromIterator(CSVParser.parse(path.toFile, Config.charset, Config.csvFormat).iterator().asScala)
        .drop(1)
        .map(record => (record.get(0), record.get(1)))
    )

  private def indexSeqToHashMap(seq: IndexedSeq[(String, String)]): HashMap[String, String] =
    HashMap(seq map { a => a._1 -> a._2 }: _*)


  def createMappingContext(config: Config): AppProgram[MappingContext] = {
    for {
      userMappingStream <- read(config.USERS_PATH)
      users <- AppDSL.streamAsSeq(userMappingStream)
      userMappings = indexSeqToHashMap(users)
      priorityMappingStream <- read(config.PRIORITIES_PATH)
      priorities <- AppDSL.streamAsSeq(priorityMappingStream)
      priorityMappings = indexSeqToHashMap(priorities)
      statusMappingStream <- read(config.STATUSES_PATH)
      statuses <- AppDSL.streamAsSeq(statusMappingStream)
      statusMappings = indexSeqToHashMap(statuses)
    } yield MappingContext(
      userMappings = userMappings,
      priorityMappings = priorityMappings,
      statusMappings = statusMappings
    )
  }

  private def readCSVFile(is: InputStream): HashMap[String, String] = {
    val parser = CSVParser.parse(is, Config.charset, Config.csvFormat)
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
        StorageDSL.copy(config.USERS_PATH, config.USERS_TEMP_PATH)
      )
      cybozuUsersStream <- AppDSL.fromStore(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(cybozuUsersStream)
      newCybozuUsersMap = cybozuUsers.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuUser) =>
          acc + (cybozuUser.userId -> "")
      }
      oldCybozuUserMap <- getOldRecords(config.USERS_TEMP_PATH)
      mergedCybozuUserMap = DiffPatch.applyChanges(oldCybozuUserMap, newCybozuUsersMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          config.USERS_PATH,
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(mergedCybozuUserMap).iterator
          )
        )
      )
      backlogUserStream <- AppDSL.fromStore(StoreDSL.getBacklogUsers)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(config.BACKLOG_USER_PATH,
          Observable(CSVRecordSerializer.backlogHeader("User")) ++
          backlogUserStream.map(user => CSVRecordSerializer.serialize(user))
        )
      )
    } yield ()
  }

  private def writePriorityMapping(config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.PRIORITIES_PATH, config.PRIORITIES_TEMP_PATH)
      )
      cybozuPrioritiesStream <- AppDSL.fromStore(StoreDSL.getCybozuPriorities)
      cybozuPriorities <- AppDSL.streamAsSeq(cybozuPrioritiesStream)
      newCybozuPrioritiesMap = cybozuPriorities.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuPriority) =>
          acc + (cybozuPriority.value -> "")
      }
      oldCybozuPriorityMap <- getOldRecords(config.PRIORITIES_TEMP_PATH)
      mergedCybozuPriorityMap = DiffPatch.applyChanges(oldCybozuPriorityMap, newCybozuPrioritiesMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          config.PRIORITIES_PATH,
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(mergedCybozuPriorityMap).iterator
          )
        )
      )
      backlogPriorityStream <- AppDSL.fromStore(StoreDSL.getBacklogPriorities)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(config.BACKLOG_PRIORITY_PATH,
          Observable(CSVRecordSerializer.backlogHeader("Priority")) ++
          backlogPriorityStream.map(priority => CSVRecordSerializer.serialize(priority))
        )
      )
    } yield ()

  private def writeStatusMapping(config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.STATUSES_PATH, config.STATUSES_TEMP_PATH)
      )
      cybozuStatusesStream <- AppDSL.fromStore(StoreDSL.getCybozuStatuses)
      cybozuStatuses <- AppDSL.streamAsSeq(cybozuStatusesStream)
      newCybozuStatusesMap = cybozuStatuses.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuStatus) =>
          acc + (cybozuStatus.value -> "")
      }
      oldCybozuStatusesMap <- getOldRecords(config.STATUSES_TEMP_PATH)
      mergedCybozuStatusMap = DiffPatch.applyChanges(oldCybozuStatusesMap, newCybozuStatusesMap)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(
          config.STATUSES_PATH,
          Observable(CSVRecordSerializer.header) ++
            Observable.fromIterator(
              CSVRecordSerializer.serializeMap(mergedCybozuStatusMap).iterator
            )
        )
      )
      backlogStatusStream <- AppDSL.fromStore(StoreDSL.getBacklogStatuses)
      _ <- AppDSL.fromStorage(
        StorageDSL.writeNewFile(config.BACKLOG_STATUS_PATH,
          Observable(CSVRecordSerializer.backlogHeader("Status")) ++
            backlogStatusStream.map(status => CSVRecordSerializer.serialize(status))
        )
      )
    } yield ()

}

object MappingFileConsole extends Logger {

  private val mappingResultBorder: AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print("\n--------------------------------------------------")
    )

  def show(config: Config): AppProgram[Unit] = {
    import MappingFiles._

    for {
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.user")) + "\n"))
      userStream <- read(config.USERS_PATH)
      _ <- AppDSL.consumeStream(
        userStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.priority")) + "\n"))
      priorityStream <- read(config.PRIORITIES_PATH)
      _ <- AppDSL.consumeStream(
        priorityStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.status")) + "\n"))
      statusStream <- read(config.STATUSES_PATH)
      _ <- AppDSL.consumeStream(
        statusStream.map(mappingResult)
      )
      _ <- mappingResultBorder
    } yield ()
  }

  private def mappingResult(row: (String, String)): AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print(s"${row._1} => ${row._2}")
    )

}