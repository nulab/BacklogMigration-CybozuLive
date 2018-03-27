package com.nulabinc.backlog.c2b.persistence.interpreters.file

import java.nio.file.Files

import com.nulabinc.backlog.c2b.persistence.dsl.{DeleteFile, ReadFile, StorageADT, WriteFile}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StorageInterpreter
import monix.eval.Task
import monix.reactive.Observable

class LocalStorageInterpreter extends StorageInterpreter {

  private val chunckSize = 8192

  override def run[A](prg: StorageProgram[A]): Task[A] =
    prg.foldMap(this)

  override def apply[A](fa: StorageADT[A]): Task[A] = fa match {
    case ReadFile(path) => {
      Task.deferAction { implicit scheduler =>
        Task.eval {
          val is = Files.newInputStream(path)

          Observable.fromInputStream(is)
        }
      }
    }
    case WriteFile(path, writeStream) =>
      Task.deferAction { implicit scheduler =>
        Task.fromFuture {
          val os = Files.newOutputStream(path)

          writeStream.foreach { bytes =>
            os.write(bytes)
          }
        }.map(_ => ())
      }
    case DeleteFile(path) => Task {
      path.toFile.delete()
    }
  }
}
