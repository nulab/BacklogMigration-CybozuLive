package com.nulabinc.backlog.c2b.persistence.interpreters

import java.io.InputStream
import java.nio.file.Path

import cats.~>
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import monix.reactive.Observable


trait StorageInterpreter[F[_]] extends (StorageADT ~> F) {

  def run[A](prg: StorageProgram[A]): F[A]

  def read[A](path: Path, f: InputStream => A): F[A]

  def writeNew(path: Path, writeStream: Observable[Array[Byte]]): F[Unit]

  def writeAppend(path: Path, writeStream: Observable[Array[Byte]]): F[Unit]

  def delete(path: Path): F[Boolean]

  def exists(path: Path): F[Boolean]

  def copy(from: Path, to: Path): F[Boolean]

  def createDirectory(path: Path): F[Unit]

  def deleteDirectory(path: Path): F[Unit]

  override def apply[A](fa: StorageADT[A]): F[A] = fa match {
    case ReadFile(path, f) => read(path, f)
    case WriteNewFile(path, writeStream) => writeNew(path, writeStream)
    case WriteAppendFile(path, writeStream) => writeAppend(path, writeStream)
    case DeleteFile(path) => delete(path)
    case Exists(path) => exists(path)
    case Copy(from, to) => copy(from, to)
    case CreateDirectory(path) => createDirectory(path)
    case DeleteDirectory(path) => deleteDirectory(path)
  }
}
