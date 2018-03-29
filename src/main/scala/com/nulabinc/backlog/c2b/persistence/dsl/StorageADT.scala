package com.nulabinc.backlog.c2b.persistence.dsl

import java.nio.file.Path

import monix.reactive.Observable

sealed trait StorageADT[A]
case class ReadFile(path: Path) extends StorageADT[Observable[Array[Byte]]]
case class WriteNewFile(path: Path, writeStream: Observable[Array[Byte]]) extends StorageADT[Unit]
case class WriteAppendFile(path: Path, writeStream: Observable[Array[Byte]]) extends StorageADT[Unit]
case class DeleteFile(path: Path) extends StorageADT[Boolean]
case class Exists(path: Path) extends StorageADT[Boolean]
case class Copy(from: Path, to: Path) extends StorageADT[Boolean]
