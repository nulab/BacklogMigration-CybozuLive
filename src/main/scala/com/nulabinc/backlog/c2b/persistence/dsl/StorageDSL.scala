package com.nulabinc.backlog.c2b.persistence.dsl

import java.io.InputStream
import java.nio.file.Path

import cats.free.Free
import monix.reactive.Observable

object StorageDSL {

  type StorageProgram[A] = Free[StorageADT, A]

  def readFile[A](path: Path, f: InputStream => A): StorageProgram[A] =
    Free.liftF(ReadFile(path, f))

  def writeNewFile(path: Path, writeStream: Observable[Array[Byte]]): StorageProgram[Unit] =
    Free.liftF(WriteNewFile(path, writeStream))

  def writeAppendFile(path: Path, writeStream: Observable[Array[Byte]]): StorageProgram[Unit] =
    Free.liftF(WriteNewFile(path, writeStream))

  def deleteFile(path: Path): StorageProgram[Boolean] =
    Free.liftF(DeleteFile(path))

  def exists(path: Path): StorageProgram[Boolean] =
    Free.liftF(Exists(path))

  def copy(from: Path, to: Path): StorageProgram[Boolean] =
    Free.liftF(Copy(from, to))

  def createDirectory(path: Path): StorageProgram[Unit] =
    Free.liftF(CreateDirectory(path))

  def deleteDirectory(path: Path): StorageProgram[Unit] =
    Free.liftF(DeleteDirectory(path))

}
