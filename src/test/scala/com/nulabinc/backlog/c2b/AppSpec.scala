package com.nulabinc.backlog.c2b

import java.io.{File, InputStream}
import java.nio.file.Path

import com.github.chaabaj.backlog4s.apis.AllApi
import com.github.chaabaj.backlog4s.dsl.BacklogHttpOp.HttpF
import com.github.chaabaj.backlog4s.dsl.{BacklogHttpInterpret, HttpQuery}
import com.github.chaabaj.backlog4s.dsl.HttpADT.{ByteStream, Response}
import cats.Monad
import com.nulabinc.backlog.c2b.datas.{CybozuEvent, CybozuForum, CybozuTodo}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.interpreters.{AppInterpreter, ConsoleInterpreter}
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageADT, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.{StorageInterpreter, StoreInterpreter}
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.{FlatSpec, Matchers}
import spray.json.JsonFormat

import scala.concurrent.Future
import scala.util.Try

class AppSpec extends FlatSpec with Matchers {

  implicit val exc: Scheduler = monix.execution.Scheduler.Implicits.global

  val backlogApi = AllApi.accessKey("https://test.com/api/v2/", "someapikey")
  val config = Config()

  val appInterpreter = new AppInterpreter(
    backlogInterpreter = new TestBacklogInterpreter,
    storageInterpreter = new TestStorageInterpreter,
    storeInterpreter = new TestStoreInterpreter,
    consoleInterpreter = new TestConsoleInterpreter
  )

  "App" should "validationProgram" in {

    val program = Validations.backlogProgram(config, backlogApi.spaceApi)

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

  class TestStorageInterpreter extends StorageInterpreter[Task] {
    override def run[A](prg: StorageProgram[A]): Task[A] = ???
    override def apply[A](fa: StorageADT[A]): Task[A] = ???
    override def read[A](path: Path, f: InputStream => A): Task[A] = ???
    override def delete(path: Path): Task[Boolean] = ???
    override def exists(path: Path): Task[Boolean] = ???
    override def writeNew(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] = ???
    override def writeAppend(path: Path, writeStream: Observable[Array[Byte]]): Task[Unit] = ???
    override def copy(from: Path, to: Path): Task[Boolean] = ???
    override def createDirectory(path: Path): Task[Unit] = ???
    override def deleteDirectory(path: Path): Task[Unit] = ???
  }

  class TestStoreInterpreter extends StoreInterpreter[Task] {
    override def run[A](prg: StoreProgram[A]): Task[A] = ???
    override def apply[A](fa: StoreADT[A]): Task[A] = ???
    override def getTodo(id: AnyId): Task[Option[CybozuTodo]] = ???
    override def getEvent(id: AnyId): Task[Option[CybozuEvent]] = ???
    override def getForum(id: AnyId): Task[Option[CybozuForum]] = ???
    override def getTodoCount: Task[AnyId] = ???
    override def getEventCount: Task[AnyId] = ???
    override def getForumCount: Task[AnyId] = ???
    override def createDatabase: Task[Unit] = ???
  }

  class TestConsoleInterpreter extends ConsoleInterpreter {
    override def read(printMessage: String): Task[String] = super.read(printMessage)
    override def print(string: String): Task[Unit] = super.print(string)
  }
}
