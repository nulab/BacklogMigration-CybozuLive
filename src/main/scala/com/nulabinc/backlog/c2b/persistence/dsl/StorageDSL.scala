package com.nulabinc.backlog.c2b.persistence.dsl

import java.nio.file.Path

import cats.free.Free
import monix.reactive.Observable

object StorageDSL {

  type StorageProgram[A] = Free[StorageADT, A]

  def readFile(path: Path): StorageProgram[Observable[Array[Byte]]] =
    Free.liftF(ReadFile(path))

  def writeFile(path: Path, writeStream: Observable[Array[Byte]]): StorageProgram[Unit] =
    Free.liftF(WriteFile(path, writeStream))

  def deleteFile(path: Path): StorageProgram[Boolean] =
    Free.liftF(DeleteFile(path))

  def exists(path: Path): StorageProgram[Boolean] =
    Free.liftF(Exists(path))

}
