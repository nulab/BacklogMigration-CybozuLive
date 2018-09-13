package com.nulabinc.backlog.c2b.interpreters

import akka.actor.ActorSystem
import akka.http.scaladsl.{ClientTransport, Http}
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import akka.stream.Materializer
import cats.~>
import com.nulabinc.backlog.c2b.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.dsl.{Get, HttpADT, RequestError, ServerDown}
import com.nulabinc.backlog.c2b.dsl.HttpDSL.HttpProgram
import monix.eval.Task
import monix.execution.Scheduler

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.higherKinds

trait HttpInterpreter[F[_]] extends (HttpADT ~> F) {

  def run[A](program: HttpProgram[A]): Task[A]

  def get(uri: String): F[Response[String]]

}

case class AkkaHttpInterpreter(optTransport: Option[ClientTransport] = None)
                              (implicit actorSystem: ActorSystem, mat: Materializer, exc: Scheduler)
  extends HttpInterpreter[Task] {

  private val settings = optTransport.map { transport =>
    ConnectionPoolSettings(actorSystem).withConnectionSettings(
      ClientConnectionSettings(actorSystem).withTransport(transport)
    )
  }.getOrElse(ConnectionPoolSettings(actorSystem))

  private val http = Http()
  private val timeout = 10.seconds
  private val maxRedirCount = 20
  private val reqHeaders: Seq[HttpHeader] = Seq(
    headers.`Accept-Charset`(HttpCharsets.`UTF-8`)
  )

  def run[A](program: HttpProgram[A]): Task[A] =
    program.foldMap(this)

  def get(uri: String): Task[Response[String]] = {
    val request = createRequest(HttpMethods.GET, uri)
    Task.deferFuture(doRequest(request))
  }

  /**
    * shutdown all connection pools
    * @return
    */
  def terminate(): Task[Unit] = Task.deferFuture {
    http.shutdownAllConnectionPools()
  }

  override def apply[A](fa: HttpADT[A]): Task[A] = fa match {
    case Get(uri) => get(uri)
  }

  private def createRequest(method: HttpMethod, uri: String): HttpRequest =
    HttpRequest(method = method, uri = uri).withHeaders(reqHeaders)

  private def doRequest(request: HttpRequest): Future[Response[String]] = {
    for {
      response <- http.singleRequest(request, settings = settings)
      data <- response.entity.toStrict(timeout).map(_.data.utf8String)
      result = {
        val status = response.status.intValue()
        if (response.status.isFailure()) {
          if (status >= 400 && status < 500)
            Left(RequestError(data))
          else {
            Left(ServerDown)
          }
        } else {
          Right(data)
        }
      }
    } yield result
  }

}