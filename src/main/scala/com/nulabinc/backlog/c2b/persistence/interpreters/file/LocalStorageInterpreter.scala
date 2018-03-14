package com.nulabinc.backlog.c2b.persistence.interpreters.file

import com.nulabinc.backlog.c2b.persistence.dsl.{ReadFile, StorageADT, WriteFile}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StorageInterpreter
import monix.eval.Task
import monix.nio.file._

class LocalStorageInterpreter extends StorageInterpreter {

  private val chunckSize = 8192

  override def run[A](prg: StorageProgram[A]): Task[A] =
    prg.foldMap(this)

  override def apply[A](fa: StorageADT[A]): Task[A] = fa match {
    case ReadFile(path) => {
      Task.deferAction { implicit scheduler =>
        Task.eval {
          readAsync(path, chunckSize)
        }
      }
    }
    case WriteFile(path, writeStream) =>
      Task.deferAction { implicit scheduler =>
        Task.eval {
          writeStream.consumeWith(writeAsync(path))
        }.map(_ => ())
      }
  }
}
