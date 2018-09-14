package com.nulabinc.backlog.c2b.interpreters

import java.io.PrintStream
import java.util.Locale

import better.files.File
import cats.~>
import com.github.chaabaj.backlog4s.dsl.BacklogHttpInterpret
import com.github.chaabaj.backlog4s.interpreters.AkkaHttpInterpret
import com.github.chaabaj.backlog4s.streaming.ApiStream.ApiStream
import com.nulabinc.backlog.c2b.dsl._
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
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


class AppInterpreter(backlogInterpreter: BacklogHttpInterpret[Future],
                     storageInterpreter: StorageInterpreter[Task],
                     storeInterpreter: StoreInterpreter[Task],
                     consoleInterpreter: ConsoleInterpreter,
                     httpInterpreter: HttpInterpreter[Task])
                    (implicit exc: Scheduler) extends (AppADT ~> Task) {

  def run[A](appProgram: AppProgram[A]): Task[A] =
    appProgram.foldMap(this)

  def export[A](file: File, content: String) = Task {
    IOUtil.output(file, content)
  }

  def `import`(config: BacklogApiConfiguration): Task[PrintStream] = Task {
    Boot.execute(config, false)
  }

  def exit(exitCode: Int): Task[Unit] =
    terminate().map { _ =>
      AnsiConsole.systemUninstall()
      sys.exit(exitCode)
    }

  def fromBacklogStream[A](stream: ApiStream[A]): Task[Observable[Seq[A]]] = Task.eval {
    Observable.fromReactivePublisher[Seq[A]](
      (s: Subscriber[_ >: Seq[A]]) => {
        backlogInterpreter.runStream(
          stream.map { value =>
            s.onNext(value) // publish data
            Seq(value)
          }
        )
        .onComplete {
          case Success(_) => s.onComplete()
          case Failure(ex) => s.onError(ex)
        }
      }
    )
  }

  def setLanguage(lang: String): Task[Unit] = Task {
    lang match {
      case "ja" => Locale.setDefault(Locale.JAPAN)
      case "en" => Locale.setDefault(Locale.US)
      case _ => ()
    }
  }

  def consumeStream(programs: Observable[AppProgram[Unit]]): Task[Unit] =
    programs.consumeWith(
      Consumer.foreachParallelTask[AppProgram[Unit]](1) { prg =>
        prg.foldMap(this).map(_ => ())
      }
    )

  def terminate(): Task[Unit] = {
    Task.deferFuture {
      backlogInterpreter match {
        case akkaInterpreter: AkkaHttpInterpret =>
          akkaInterpreter.terminate()
        case _ =>
          Future.successful()
      }
    }
    httpInterpreter match {
      case httpInterpreter: AkkaHttpInterpreter =>
        httpInterpreter.terminate()
      case _ =>
        Task()
    }
  }

  override def apply[A](fa: AppADT[A]): Task[A] = fa match {
    case Pure(a) =>
      Task(a)
    case FromTask(task) =>
      task
      .onErrorHandle(ex =>Failure(ex))
      .map {
        case Success(data) => Success(data)
        case Failure(error) => Failure(error)
        case data => Success(data)
      }
    case FromStorage(storePrg) =>
      storageInterpreter.run(storePrg)
    case FromDB(dbPrg) =>
      storeInterpreter.run(dbPrg)
    case FromConsole(consolePrg) =>
      consoleInterpreter.run(consolePrg)
    case FromBacklog(backlogPrg) => Task.deferFuture {
      backlogInterpreter.run(backlogPrg)
    }
    case FromBacklogStream(stream) =>
      fromBacklogStream(stream)
    case FromHttp(program) =>
      httpInterpreter.run(program)
    case ConsumeStream(prgs) =>
      consumeStream(prgs)
    case Export(file, content) =>
      export(file, content)
    case Import(config) =>
      `import`(config)
    case SetLanguage(lang) =>
      setLanguage(lang)
  }
}
