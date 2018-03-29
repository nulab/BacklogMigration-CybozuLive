package com.nulabinc.backlog.c2b.services

import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.generators.CSVRecordSerializer
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import monix.reactive.Observable

object MappingFiles {

  import CSVRecordSerializer._

  private def writeUserMapping(config: Config): AppProgram[Unit] = {
    for {
      backlogUsers <- AppDSL.fromDB(StoreDSL.getBacklogUsers)
      cybozuUsers <- AppDSL.fromDB(StoreDSL.getCybozuUsers)
      backlogUserStream = backlogUsers.map { backlogUser =>
        CSVRecordSerializer.serialize(backlogUser)
      }
      cybozuUserStream = cybozuUsers.map { cybozuUser =>
        CSVRecordSerializer.serialize(cybozuUser)
      }
      _ <- AppDSL.fromStorage(
        StorageDSL.writeAppendFile(config.USERS_PATH,
          Observable(CSVRecordSerializer.header) ++
          backlogUserStream ++
          Observable(CSVRecordSerializer.split) ++
          cybozuUserStream
        )
      )
    } yield ()
  }

  private def writePriorityMapping(config: Config): AppProgram[Unit] = {
    for {
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

  def write(config: Config): AppProgram[Unit] =
    for {
      _ <- writeUserMapping(config)
      _ <- writePriorityMapping(config)
      _ <- writeStatusMapping(config)

    } yield ()
}
