package com.nulabinc.backlog.c2b.persistence.interpreters

import java.nio.file.Path

import cats.~>
import com.nulabinc.backlog.c2b.persistence.dsl.StorageADT
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import monix.eval.Task
import monix.reactive.Observable


trait StorageInterpreter[F[_]] extends (StorageADT ~> F) {

  def run[A](prg: StorageProgram[A]): F[A]

  def read(path: Path): F[Observable[Array[Byte]]]

  def write(path: Path, writeStream: Observable[Array[Byte]]): F[Unit]

  def delete(path: Path): F[Boolean]

  def exists(path: Path): F[Boolean]
}
