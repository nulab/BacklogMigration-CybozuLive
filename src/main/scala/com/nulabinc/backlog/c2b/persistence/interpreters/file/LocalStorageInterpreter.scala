package com.nulabinc.backlog.c2b.persistence.interpreters.file

import java.io.InputStream
import java.nio.file.{Files, Path, StandardOpenOption}

import better.files.File
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StorageInterpreter
import monix.eval.Task
import monix.reactive.Observable

import scala.util.Try
import scala.util.control.NonFatal

class LocalStorageInterpreter extends StorageInterpreter[Task] {

  private val chunckSize = 8192

  override def run[A](prg: StorageProgram[A]): Task[A] =
    prg.foldMap(this)

  override def read[A](path: Path, f: InputStream => A): Task[A] =
    Task.deferAction { implicit scheduler =>
      Task.eval {
        val is = Files.newInputStream(path)
        Try(f(is))
          .map { result =>
            is.close()
            result
          }
          .recover {
          case NonFatal(ex) =>
            is.close()
            throw ex
        }.get
      }
    }

  override def writeNew(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] =
    for {
      _ <- delete(path)
      _ <- write(path, writeStream, StandardOpenOption.CREATE)
    } yield ()

  override def writeAppend(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] =
    write(path, writeStream, StandardOpenOption.APPEND)

  override def delete(path: Path): Task[Boolean] =
    exists(path).map { result =>
      if (result) {
        path.toFile.delete()
      } else {
        false
      }
    }

  override def exists(path: Path): Task[Boolean] = Task {
    path.toFile.exists()
  }

  override def copy(from: Path, to: Path): Task[Boolean] =
    exists(from).flatMap { result =>
      if (result) {
        delete(to).map { _ =>
          Files.move(from, to)
          true
        }
      } else {
        Task(false)
      }
    }

  override def createDirectory(path: Path): Task[Unit] =
    exists(path).map { result =>
      if (!result) {
        File(path).createDirectory()
      }
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
