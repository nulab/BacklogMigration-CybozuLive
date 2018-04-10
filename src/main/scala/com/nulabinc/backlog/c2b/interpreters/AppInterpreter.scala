package com.nulabinc.backlog.c2b.interpreters

import java.io.PrintStream
import java.util.Locale

import better.files.File
import cats.free.Free
import cats.~>
import com.github.chaabaj.backlog4s.dsl.ApiDsl.ApiPrg
import com.github.chaabaj.backlog4s.dsl.BacklogHttpInterpret
import com.github.chaabaj.backlog4s.interpreters.AkkaHttpInterpret
import com.github.chaabaj.backlog4s.streaming.ApiStream.ApiStream
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL.ConsoleProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters._
import com.nulabinc.backlog.migration.common.conf.BacklogApiConfiguration
import com.nulabinc.backlog.migration.common.utils.IOUtil
import com.nulabinc.backlog.migration.importer.core.Boot
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.{Consumer, Observable}
import org.fusesource.jansi.AnsiConsole
import org.reactivestreams.Subscriber

import scala.concurrent.Future
import scala.util.{Failure, Success}

sealed trait AppADT[+A]
case class Pure[A](a: A) extends AppADT[A]
case class FromStorage[A](prg: StorageProgram[A]) extends AppADT[A]
case class FromDB[A](prg: StoreProgram[A]) extends AppADT[A]
case class FromConsole[A](prg: ConsoleProgram[A]) extends AppADT[A]
case class FromBacklog[A](prg: ApiPrg[A]) extends AppADT[A]
case class FromBacklogStream[A](prg: ApiStream[A]) extends AppADT[Observable[Seq[A]]]
case class Exit(exitCode: Int) extends AppADT[Unit]
case class ConsumeStream(prgs: Observable[AppProgram[Unit]]) extends AppADT[Unit]
private case class FromTask[A](task: Task[A]) extends AppADT[A]
case class SetLanguage(lang: String) extends AppADT[Unit]
case class Export(file: File, content: String) extends AppADT[File]
case class Import(backlogApiConfiguration: BacklogApiConfiguration) extends AppADT[PrintStream]

object AppDSL {

  type AppProgram[A] = Free[AppADT, A]

  def pure[A](a: A): AppProgram[A] =
    Free.liftF(Pure(a))

  def consumeStream[A](prgs: Observable[AppProgram[Unit]]): AppProgram[Unit] =
    Free.liftF[AppADT, Unit](ConsumeStream(prgs))

  private def fromTask[A](task: Task[A]): AppProgram[A] =
    Free.liftF(FromTask(task))

  def foldLeftStream[A, B](stream: Observable[A], zero: B)(f: (B, A) => B): AppProgram[B] =
    fromTask(stream.foldLeftL(zero)(f))

  def streamAsSeq[A](stream: Observable[A]): AppProgram[IndexedSeq[A]] = {
    foldLeftStream(stream, IndexedSeq.empty[A]) {
      case (acc, item) =>
        acc :+ item
    }
  }

  def fromDB[A](dbProgram: StoreProgram[A]): AppProgram[A] =
    Free.liftF(FromDB(dbProgram))

  def fromStorage[A](storageProgram: StorageProgram[A]): AppProgram[A] =
    Free.liftF(FromStorage(storageProgram))

  def fromConsole[A](consoleProgram: ConsoleProgram[A]): AppProgram[A] =
    Free.liftF(FromConsole(consoleProgram))

  def fromBacklog[A](backlogProgram: ApiPrg[A]): AppProgram[A] =
    Free.liftF(FromBacklog(backlogProgram))

  def fromBacklogStream[A](prg: ApiStream[A]): AppProgram[Observable[Seq[A]]] =
    Free.liftF[AppADT, Observable[Seq[A]]](FromBacklogStream(prg))

  def exit(reason: String, exitCode: Int): AppProgram[Unit] =
    for {
      _ <- fromConsole(ConsoleDSL.print(reason))
      _ <- Free.liftF(Exit(exitCode))
    } yield ()

  def setLanguage(lang: String): AppProgram[Unit] =
    Free.liftF(SetLanguage(lang))

  def export(message: String, file: File, content: String): AppProgram[File] =
    for {
      _ <- fromConsole(ConsoleDSL.print(message))
      file <- Free.liftF(Export(file, content))
    } yield file

  def `import`(backlogApiConfiguration: BacklogApiConfiguration): AppProgram[PrintStream] =
    Free.liftF(Import(backlogApiConfiguration))
}

class AppInterpreter(backlogInterpreter: BacklogHttpInterpret[Future],
                     storageInterpreter: StorageInterpreter[Task],
                     storeInterpreter: StoreInterpreter[Task],
                     consoleInterpreter: ConsoleInterpreter)
                    (implicit exc: Scheduler) extends (AppADT ~> Task) {

  def terminate(): Task[Unit] = Task.deferFuture {
    backlogInterpreter match {
      case akkaInterpreter: AkkaHttpInterpret =>
        akkaInterpreter.terminate()
      case _ =>
        Future.successful()
    }

  }

  def run[A](appProgram: AppProgram[A]): Task[A] =
    appProgram.foldMap(this)

  def setLanguage(lang: String): Task[Unit] = Task { // TODO: change to Locale
    lang match {
      case "ja" => Locale.setDefault(Locale.JAPAN)
      case "en" => Locale.setDefault(Locale.US)
      case _ => ()
    }
  }

  override def apply[A](fa: AppADT[A]): Task[A] = fa match {
    case Pure(a) => Task(a)
    case FromStorage(storePrg) =>
      storageInterpreter.run(storePrg)
    case FromDB(dbPrg) =>
      storeInterpreter.run(dbPrg)
    case FromConsole(consolePrg) =>
      consoleInterpreter.run(consolePrg)
    case FromBacklog(backlogPrg) => Task.deferFuture {
      backlogInterpreter.run(backlogPrg)
    }
    case FromBacklogStream(stream) => Task.eval {
      Observable.fromReactivePublisher[Seq[A]](
        (s: Subscriber[_ >: Seq[A]]) => {
          backlogInterpreter.runStream(
            stream.map { value =>
              val a = value.asInstanceOf[Seq[A]]
              s.onNext(a) // publish data
              Seq(a)
            }
          )
          .onComplete {
            case Success(_) => s.onComplete()
            case Failure(ex) => s.onError(ex)
          }
        }
      )
    }
    case ConsumeStream(prgs) =>
      prgs.consumeWith(
        Consumer.foreachParallelTask[AppProgram[Unit]](1) { prg =>
          prg.foldMap(this).map(_ => ())
        }
      )
    case FromTask(task) => task
    case Exit(statusCode) => Task {
      AnsiConsole.systemUninstall()
      sys.exit(statusCode)
    }
    case SetLanguage(lang: String) =>
      setLanguage(lang)
    case Export(file, content) => Task {
      IOUtil.output(file, content)
    }
    case Import(config) => Task {
      Boot.execute(config, false)
    }
  }
}
