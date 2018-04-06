package com.nulabinc.backlog.c2b.persistence.dsl

import java.io.InputStream
import java.nio.file.Path

import monix.reactive.Observable

sealed trait StorageADT[A]
case class ReadFile[A](path: Path, f: (InputStream) => A) extends StorageADT[A]
case class WriteNewFile(path: Path, writeStream: Observable[Array[Byte]]) extends StorageADT[Unit]
case class WriteAppendFile(path: Path, writeStream: Observable[Array[Byte]]) extends StorageADT[Unit]
case class DeleteFile(path: Path) extends StorageADT[Boolean]
case class Exists(path: Path) extends StorageADT[Boolean]
case class Copy(from: Path, to: Path) extends StorageADT[Boolean]
case class CreateDirectory(path: Path) extends StorageADT[Unit]
case class DeleteDirectory(path: Path) extends StorageADT[Unit]
