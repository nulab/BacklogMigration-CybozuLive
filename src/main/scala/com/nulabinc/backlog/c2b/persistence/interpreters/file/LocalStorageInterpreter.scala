package com.nulabinc.backlog.c2b.persistence.interpreters.file

import java.nio.file.{Files, Path}

import com.nulabinc.backlog.c2b.persistence.dsl.{DeleteFile, ReadFile, StorageADT, WriteFile}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StorageInterpreter
import monix.eval.Task
import monix.reactive.Observable

class LocalStorageInterpreter extends StorageInterpreter[Task] {

  private val chunckSize = 8192

  override def run[A](prg: StorageProgram[A]): Task[A] =
    prg.foldMap(this)

  override def read(path: Path): Task[Observable[Array[Byte]]] =
    Task.deferAction { implicit scheduler =>
      Task.eval {
        val is = Files.newInputStream(path)
        Observable.fromInputStream(is)
      }
    }

  override def write(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] =
    Task.deferAction { implicit scheduler =>
      Task.fromFuture {
        val os = Files.newOutputStream(path)
        writeStream.foreach { bytes =>
          os.write(bytes)
        }
      }.map(_ => ())
    }

  override def delete(path: Path): Task[Boolean] = Task {
    path.toFile.delete()
  }

  override def apply[A](fa: StorageADT[A]): Task[A] = fa match {
    case ReadFile(path) => read(path)
    case WriteFile(path, writeStream) => write(path, writeStream)
    case DeleteFile(path) => delete(path)
  }

}
