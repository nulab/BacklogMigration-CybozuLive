package com.nulabinc.backlog.c2b.services

import java.io.InputStream
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.dsl.AppDSL
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.serializers.{CSVRecordSerializer, Serializer}
import com.nulabinc.backlog.c2b.services.DiffPatch.DiffMap
import com.osinka.i18n.Messages
import monix.reactive.Observable
import org.apache.commons.csv.CSVParser

import scala.collection.immutable.HashMap

object MappingFiles {
  import com.nulabinc.backlog.c2b.syntax.AppProgramOps._
  import CSVRecordSerializer._

  private val charset = Config.mappingFileCharset

  def write(config: Config): AppProgram[Unit] =
    for {
      _ <- writeUserMapping()
      _ <- writePriorityMapping()
      _ <- writeStatusMapping()
    } yield ()

  def read(path: Path): AppProgram[Observable[(String, String)]] =
    AppDSL.pure(
      Observable.fromIterator(CSVParser.parse(path.toFile, charset, Config.csvFormat).iterator().asScala)
        .drop(1)
        .map(record => (record.get(0), record.get(1)))
    )

  def createMappingContext(): AppProgram[MappingContext] =
    for {
      userMappingStream <- read(Config.USERS_PATH)
      userNames <- AppDSL.streamAsSeq(userMappingStream).orFail
      backlogUserStream <- AppDSL.fromStore(StoreDSL.getBacklogUsers)
      backlogUsers <- AppDSL.streamAsSeq(backlogUserStream).orFail
      userHashMap = userNames.map {
        case (cybozu, backlog) =>
          val backlogUserId = backlogUsers
            .find(_.name == backlog)
            .getOrElse(throw new RuntimeException("It never happen. It has already validated."))
            .userId
            .getOrElse(throw new RuntimeException("It never happen. Admin user can get userId."))
          (cybozu, backlogUserId)
      }
      userMappings = indexSeqToHashMap(userHashMap)
      priorityMappingStream <- read(Config.PRIORITIES_PATH)
      priorities <- AppDSL.streamAsSeq(priorityMappingStream).orFail
      priorityMappings = indexSeqToHashMap(priorities)
      statusMappingStream <- read(Config.STATUSES_PATH)
      statuses <- AppDSL.streamAsSeq(statusMappingStream).orFail
      statusMappings = indexSeqToHashMap(statuses)
    } yield {
      MappingContext(
        userMappings = userMappings,
        priorityMappings = priorityMappings,
        statusMappings = statusMappings
      )
    }

  private def readCSVFile(is: InputStream): HashMap[String, String] = {
    val parser = CSVParser.parse(is, charset, Config.csvFormat)
    parser.getRecords.asScala.foldLeft(HashMap.empty[String, String]) {
      case (acc, record) =>
        acc + (record.get(0) -> record.get(1))
    }
  }

  private def getOldRecords(path: Path): AppProgram[HashMap[String, String]] =
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

  private def writeUserMapping(): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.USERS_PATH, Config.USERS_TEMP_PATH)
      )
      cybozuUsersStream <- AppDSL.fromStore(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(cybozuUsersStream).orFail
      newCybozuUsersMap = cybozuUsers.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuUser) =>
          acc + (cybozuUser.userId -> "")
      }
      oldCybozuUserMap <- getOldRecords(Config.USERS_TEMP_PATH)
      mergedCybozuUserMap = DiffPatch.applyChanges(oldCybozuUserMap, newCybozuUsersMap)
      _ <- writeDiffMap(Config.USERS_PATH, mergedCybozuUserMap)
      backlogUserStream <- AppDSL.fromStore(StoreDSL.getBacklogUsers)
      _ <- writeMappingFile(Config.BACKLOG_USER_PATH, "User", backlogUserStream)
    } yield ()
  }

  private def writePriorityMapping(): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.PRIORITIES_PATH, Config.PRIORITIES_TEMP_PATH)
      )
      cybozuPrioritiesStream <- AppDSL.fromStore(StoreDSL.getCybozuPriorities)
      cybozuPriorities <- AppDSL.streamAsSeq(cybozuPrioritiesStream).orFail
      newCybozuPrioritiesMap = cybozuPriorities.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuPriority) =>
          acc + (cybozuPriority.value -> "")
      }
      oldCybozuPriorityMap <- getOldRecords(Config.PRIORITIES_TEMP_PATH)
      mergedCybozuPriorityMap = DiffPatch.applyChanges(oldCybozuPriorityMap, newCybozuPrioritiesMap)
      _ <- writeDiffMap(Config.PRIORITIES_PATH, mergedCybozuPriorityMap)
      backlogPriorityStream <- AppDSL.fromStore(StoreDSL.getBacklogPriorities)
      _ <- writeMappingFile(Config.BACKLOG_PRIORITY_PATH, "Priority", backlogPriorityStream)
    } yield ()

  private def writeStatusMapping(): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(Config.STATUSES_PATH, Config.STATUSES_TEMP_PATH)
      )
      cybozuStatusesStream <- AppDSL.fromStore(StoreDSL.getCybozuStatuses)
      cybozuStatuses <- AppDSL.streamAsSeq(cybozuStatusesStream).orFail
      newCybozuStatusesMap = cybozuStatuses.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuStatus) =>
          acc + (cybozuStatus.value -> "")
      }
      oldCybozuStatusesMap <- getOldRecords(Config.STATUSES_TEMP_PATH)
      mergedCybozuStatusMap = DiffPatch.applyChanges(oldCybozuStatusesMap, newCybozuStatusesMap)
      _ <- writeDiffMap(Config.STATUSES_PATH, mergedCybozuStatusMap)
      backlogStatusStream <- AppDSL.fromStore(StoreDSL.getBacklogStatuses)
      _ <- writeMappingFile(Config.BACKLOG_STATUS_PATH, "Status", backlogStatusStream)
    } yield ()

  private def indexSeqToHashMap(seq: IndexedSeq[(String, String)]): HashMap[String, String] =
    HashMap(seq map { a => a._1 -> a._2 }: _*)

  private def mappingFileExists(path: Path): AppProgram[Boolean] =
    AppDSL.fromStorage(StorageDSL.exists(path))

  private def writeDiffMap(path: Path, diffMap: DiffMap): AppProgram[Unit] =
    AppDSL.fromStorage(
      StorageDSL.writeAppendFile(
        path = path,
        writeStream =
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(diffMap).iterator
          )
      )
    )

  private def writeMappingFile[A](path: Path,
                                  mappingFileKind: String,
                                  stream: Observable[A])
                                 (implicit serializer: Serializer[A, Array[Byte]]): AppProgram[Unit] =
    AppDSL.fromStorage(
      StorageDSL.writeNewFile(
        path = path,
        writeStream =
          Observable(CSVRecordSerializer.backlogHeader(mappingFileKind)) ++
          stream.map(value => CSVRecordSerializer.serialize(value))
      )
    )
}

object MappingFileConsole extends Logger {

  private val mappingResultBorder: AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print("\n--------------------------------------------------")
    )

  def show(): AppProgram[Unit] = {
    import MappingFiles._

    for {
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.user")) + "\n"))
      userStream <- read(Config.USERS_PATH)
      _ <- AppDSL.consumeStream(
        userStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.priority")) + "\n"))
      priorityStream <- read(Config.PRIORITIES_PATH)
      _ <- AppDSL.consumeStream(
        priorityStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.status")) + "\n"))
      statusStream <- read(Config.STATUSES_PATH)
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