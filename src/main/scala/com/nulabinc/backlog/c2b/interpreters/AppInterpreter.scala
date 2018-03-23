package com.nulabinc.backlog.c2b.interpreters

import backlog4s.dsl.ApiDsl.ApiPrg
import backlog4s.dsl.BacklogHttpInterpret
import cats.free.Free
import cats.~>
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL.ConsoleProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters._
import monix.eval.Task
import org.fusesource.jansi.AnsiConsole

import scala.concurrent.Future

sealed trait AppADT[+A]
case class Pure[A](a: A) extends AppADT[A]
case class FromStorage[A](prg: StorageProgram[A]) extends AppADT[A]
case class FromDB[A](prg: StoreProgram[A]) extends AppADT[A]
case class FromConsole[A](prg: ConsoleProgram[A]) extends AppADT[A]
case class FromBacklog[A](prg: ApiPrg[A]) extends AppADT[A]
case class Exit(exitCode: Int) extends AppADT[Unit]

object AppDSL {

  type AppProgram[A] = Free[AppADT, A]

  def pure[A](a: A): AppProgram[A] =
    Free.liftF(Pure(a))

  def fromDB[A](dbProgram: StoreProgram[A]): AppProgram[A] =
    Free.liftF(FromDB(dbProgram))

  def fromStorage[A](storageProgram: StorageProgram[A]): AppProgram[A] =
    Free.liftF(FromStorage(storageProgram))

  def fromConsole[A](consoleProgram: ConsoleProgram[A]): AppProgram[A] =
    Free.liftF(FromConsole(consoleProgram))

  def fromBacklog[A](backlogProgram: ApiPrg[A]): AppProgram[A] =
    Free.liftF(FromBacklog(backlogProgram))

  def exit(reason: String, exitCode: Int): AppProgram[Unit] = {
    for {
      _ <- fromConsole(ConsoleDSL.print(reason))
      _ <- Free.liftF(Exit(exitCode))
    } yield ()
  }
}

class AppInterpreter(backlogInterpreter: BacklogHttpInterpret[Future],
                     storageInterpreter: StorageInterpreter,
                     dbInterpreter: DBInterpreter,
                     consoleInterpreter: ConsoleInterpreter) extends (AppADT ~> Task) {

  def run[A](appProgram: AppProgram[A]): Task[A] =
    appProgram.foldMap(this)

  override def apply[A](fa: AppADT[A]): Task[A] = fa match {
    case Pure(a) => Task(a)
    case FromStorage(storePrg) =>
      storageInterpreter.run(storePrg)
    case FromDB(dbPrg) =>
      dbInterpreter.run(dbPrg)
    case FromConsole(consolePrg) =>
      consoleInterpreter.run(consolePrg)
    case FromBacklog(backlogPrg) => Task.deferFuture {
      backlogInterpreter.run(backlogPrg)
    }
    case Exit(statusCode) => Task {
      AnsiConsole.systemUninstall()
      sys.exit(statusCode)
    }
  }
}
