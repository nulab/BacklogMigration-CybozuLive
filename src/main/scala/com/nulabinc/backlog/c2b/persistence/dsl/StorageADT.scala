package com.nulabinc.backlog.c2b.persistence.dsl

import java.nio.file.Path

import monix.reactive.Observable

sealed trait StorageADT[A]
case class ReadFile(path: Path) extends StorageADT[Observable[Array[Byte]]]
case class WriteFile(path: Path, writeStream: Observable[Array[Byte]]) extends StorageADT[Unit]
