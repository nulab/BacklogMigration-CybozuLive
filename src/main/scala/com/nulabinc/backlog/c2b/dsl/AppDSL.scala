package com.nulabinc.backlog.c2b.dsl

import java.io.PrintStream

import better.files.File
import cats.free.Free
import com.github.chaabaj.backlog4s.dsl.ApiDsl.ApiPrg
import com.github.chaabaj.backlog4s.streaming.ApiStream.ApiStream
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.dsl.ConsoleDSL.ConsoleProgram
import com.nulabinc.backlog.c2b.dsl.HttpDSL.HttpProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import monix.eval.Task
import monix.reactive.Observable

import scala.util.Try

object AppDSL {

  type AppProgram[A] = Free[AppADT, A]

  def pure[A](a: A): AppProgram[A] =
    Free.liftF(Pure(a))

  val empty: AppProgram[Unit] =
    pure(())

  def consumeStream[A](prgs: Observable[AppProgram[Unit]]): AppProgram[Unit] =
    Free.liftF[AppADT, Unit](ConsumeStream(prgs))

  private def fromTask[A](task: Task[A]): AppProgram[Try[A]] =
    Free.liftF[AppADT, Try[A]](FromTask(task))

  def foldLeftStream[A, B](stream: Observable[A], zero: B)(f: (B, A) => B): AppProgram[Try[B]] =
    fromTask(stream.foldLeftL(zero)(f))

  def streamAsSeq[A](stream: Observable[A]): AppProgram[Try[IndexedSeq[A]]] = {
    foldLeftStream(stream, IndexedSeq.empty[A]) {
      case (acc, item) =>
        acc :+ item
    }
  }

  def fromStore[A](storeProgram: StoreProgram[A]): AppProgram[A] =
    Free.liftF(FromDB(storeProgram))

  def fromStorage[A](storageProgram: StorageProgram[A]): AppProgram[A] =
    Free.liftF(FromStorage(storageProgram))

  def fromConsole[A](consoleProgram: ConsoleProgram[A]): AppProgram[A] =
    Free.liftF(FromConsole(consoleProgram))

  def fromBacklog[A](backlogProgram: ApiPrg[A]): AppProgram[A] =
    Free.liftF(FromBacklog(backlogProgram))

  def fromBacklogStream[A](prg: ApiStream[A]): AppProgram[Observable[Seq[A]]] =
    Free.liftF[AppADT, Observable[Seq[A]]](FromBacklogStream(prg))

  def fromHttp[A](program: HttpProgram[A]): AppProgram[A] =
    Free.liftF(FromHttp(program))

  def export(message: String, file: File, content: String): AppProgram[File] =
    for {
      _ <- fromConsole(ConsoleDSL.print(message))
      file <- Free.liftF(Export(file, content))
    } yield file

  def `import`(backlogApiConfiguration: BacklogApiConfiguration): AppProgram[PrintStream] =
    Free.liftF(Import(backlogApiConfiguration))

  def finalizeImport(config: Config): AppProgram[Unit] =
    for {
      _ <- fromHttp(HttpDSL.get(s"${config.backlogUrl}/api/v2/importer/cybouz?projectKey=${config.projectKey}"))
    } yield ()

  def setLanguage(lang: String): AppProgram[Unit] =
    Free.liftF(SetLanguage(lang))

}
