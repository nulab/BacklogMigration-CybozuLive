package com.nulabinc.backlog.c2b.dsl

import java.io.PrintStream

import better.files.File
import com.github.chaabaj.backlog4s.dsl.ApiDsl.ApiPrg
import com.github.chaabaj.backlog4s.streaming.ApiStream.ApiStream
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.dsl.ConsoleDSL.ConsoleProgram
import com.nulabinc.backlog.c2b.dsl.HttpDSL.HttpProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import monix.eval.Task
import monix.reactive.Observable

import scala.util.Try

sealed trait AppADT[+A]
case class Pure[A](a: A) extends AppADT[A]
case class FromStorage[A](prg: StorageProgram[A]) extends AppADT[A]
case class FromDB[A](prg: StoreProgram[A]) extends AppADT[A]
case class FromConsole[A](prg: ConsoleProgram[A]) extends AppADT[A]
case class FromBacklog[A](prg: ApiPrg[A]) extends AppADT[A]
case class FromBacklogStream[A](prg: ApiStream[A]) extends AppADT[Observable[Seq[A]]]
case class FromHttp[A](program: HttpProgram[A]) extends AppADT[A]
case class ConsumeStream(prgs: Observable[AppProgram[Unit]]) extends AppADT[Unit]

case class FromTask[A](task: Task[A]) extends AppADT[Try[A]]

case class Export(file: File, content: String) extends AppADT[File]
case class Import(backlogApiConfiguration: BacklogApiConfiguration) extends AppADT[PrintStream]
