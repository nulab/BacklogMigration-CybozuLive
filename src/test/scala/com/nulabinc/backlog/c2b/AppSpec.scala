package com.nulabinc.backlog.c2b

import java.io.File

import backlog4s.apis.AllApi
import backlog4s.dsl.BacklogHttpOp.HttpF
import backlog4s.dsl.{BacklogHttpInterpret, HttpQuery}
import backlog4s.dsl.HttpADT.{ByteStream, Response}
import cats.Monad
import com.nulabinc.backlog.c2b.interpreters.{AppInterpreter, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageADT, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.{DBInterpreter, StorageInterpreter}
import monix.eval.Task
import org.scalatest.{FlatSpec, Matchers}
import spray.json.JsonFormat

import scala.concurrent.Future
import scala.util.Try

class AppSpec extends FlatSpec with Matchers {

  val backlogApi = AllApi.accessKey("https://test.com/api/v2/", "someapikey")
  val config = Config()

  val appInterpreter = new AppInterpreter(
    backlogInterpreter = new TestBacklogInterpreter,
    storageInterpreter = new TestStorageInterpreter,
    dbInterpreter = new TestDBInterpreter,
    consoleInterpreter = new TestConsoleInterpreter
  )

  "App" should "validationProgram" in {

    val program = App.validationProgram(config, backlogApi)

    appInterpreter.run(program)
  }


  class TestBacklogInterpreter extends BacklogHttpInterpret[Future] {

    implicit val monad = implicitly[Monad[Future]]

    override def get[A](query: HttpQuery, format: JsonFormat[A]): Future[Response[A]] = ???
    override def create[Payload, A](query: HttpQuery, payload: Payload, format: JsonFormat[A], payloadFormat: JsonFormat[Payload]): Future[Response[A]] = ???
    override def update[Payload, A](query: HttpQuery, payload: Payload, format: JsonFormat[A], payloadFormat: JsonFormat[Payload]): Future[Response[A]] = ???
    override def delete(query: HttpQuery): Future[Response[Unit]] = ???
    override def download(query: HttpQuery): Future[Response[ByteStream]] = ???
    override def upload[A](query: HttpQuery, file: File, format: JsonFormat[A]): Future[Response[A]] = ???
    override def pure[A](a: A): Future[A] = ???
    override def parallel[A](prgs: Seq[HttpF[A]]): Future[Seq[A]] = ???
    override def onComplete[A](l: Future[A])(f: Try[A] => Unit): Future[A] = ???
  }

  class TestStorageInterpreter extends StorageInterpreter {
    override def run[A](prg: StorageProgram[A]): Task[A] = ???
    override def apply[A](fa: StorageADT[A]): Task[A] = ???
  }

  class TestDBInterpreter extends DBInterpreter {
    override def run[A](prg: StoreProgram[A]): Task[A] = ???
    override def apply[A](fa: StoreADT[A]): Task[A] = ???
  }

  class TestConsoleInterpreter extends ConsoleInterpreter {
    override def read(printMessage: String): Task[String] = super.read(printMessage)
    override def print(string: String): Task[Unit] = super.print(string)
  }
}
