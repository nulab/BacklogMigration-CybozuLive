package com.nulabinc.backlog.c2b.persistence.interpreters.file

import java.nio.file.{Files, Path, StandardOpenOption}

import akka.actor.Status.Success
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StorageInterpreter
import monix.eval.Task
import monix.reactive.Observable

import scala.util.Failure
import scala.util.control.NonFatal

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

  override def writeNew(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] =
    write(path, writeStream, StandardOpenOption.CREATE)

  override def writeAppend(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] =
    write(path, writeStream, StandardOpenOption.APPEND)

  override def delete(path: Path): Task[Boolean] = Task {
    path.toFile.delete()
  }

  override def exists(path: Path): Task[Boolean] = Task {
    path.toFile.exists()
  }

  override def apply[A](fa: StorageADT[A]): Task[A] = fa match {
    case ReadFile(path) => read(path)
    case WriteNewFile(path, writeStream) => writeNew(path, writeStream)
    case WriteAppendFile(path, writeStream) => writeAppend(path, writeStream)
    case DeleteFile(path) => delete(path)
    case Exists(path) => exists(path)
  }

  private def write(path: Path, writeStream: Observable[Array[Byte]], option: StandardOpenOption) =
    Task.deferAction { implicit scheduler =>
      Task.fromFuture {
        val os = Files.newOutputStream(path, option)
        writeStream.foreach { bytes =>
          os.write(bytes)
        }.map(_ => os.close())
          .recover {
            case NonFatal(ex) =>
              ex.printStackTrace()
              os.close()
          }
      }.map(_ => ())
    }

}
