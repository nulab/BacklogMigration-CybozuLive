package com.nulabinc.backlog.c2b.services

import java.io.InputStream
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.serializers.CSVRecordSerializer
import com.osinka.i18n.Messages
import monix.reactive.Observable
import org.apache.commons.csv.CSVParser

import scala.collection.immutable.HashMap
import scala.util.{Try, Success, Failure}

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
    } yield {
      MappingContext(
        userMappings = userMappings,
        priorityMappings = priorityMappings,
        statusMappings = statusMappings
      )
    }
  }

  implicit class TryProgramOps[A](result: Try[AppProgram[A]]) {
    def sequence: AppProgram[Try[A]] =
      result match {
        case Success(data) => data.map(Success(_))
        case Failure(error) => AppDSL.pure(Failure(error))
      }
  }

  private def readCSVFile(is: InputStream): HashMap[String, String] = {
    val parser = CSVParser.parse(is, Config.charset, Config.csvFormat)
    parser.getRecords.asScala.foldLeft(HashMap.empty[String, String]) {
      case (acc, record) =>
        acc + (record.get(0) -> record.get(1))
    }
  }

  private def getOldRecords(path: Path): AppProgram[Try[HashMap[String, String]]] = Try {
    for {
      oldExists <- mappingFileExists(path)
      oldRecords <- if (oldExists) {
        AppDSL.fromStorage(
          StorageDSL.readFile(path, readCSVFile)
        )
      } else {
        AppDSL.pure(HashMap.empty[String, String])
      }
    } yield oldRecords
  }.sequence

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
      oldCybozuUserMapResult <- getOldRecords(config.USERS_TEMP_PATH)
      _ <- oldCybozuUserMapResult match {
        case Success(data) =>
          val mergedCybozuUserMap = DiffPatch.applyChanges(data, newCybozuUsersMap)
          for {
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
        case Failure(_) =>
          AppDSL.exit("Invalid CSV", 1)
      }
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
      oldCybozuPriorityMapResult <- getOldRecords(config.PRIORITIES_TEMP_PATH)
      _ <- oldCybozuPriorityMapResult match {
        case Success(data) =>
          val mergedCybozuPriorityMap = DiffPatch.applyChanges(data, newCybozuPrioritiesMap)
          for {
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
        case Failure(_) =>
          AppDSL.exit("Invalid CSV", 1)
      }
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
      oldCybozuStatusesMapResult <- getOldRecords(config.STATUSES_TEMP_PATH)
      _ <- oldCybozuStatusesMapResult match {
        case Success(data) =>
          val mergedCybozuStatusMap = DiffPatch.applyChanges(data, newCybozuStatusesMap)
          for {
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
        case Failure(_) =>
          AppDSL.exit("Invalid CSV", 1)
      }
    } yield ()

  private def indexSeqToHashMap(seq: IndexedSeq[(String, String)]): HashMap[String, String] =
    HashMap(seq map { a => a._1 -> a._2 }: _*)

  private def mappingFileExists(path: Path): AppProgram[Boolean] =
    AppDSL.fromStorage(StorageDSL.exists(path))
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