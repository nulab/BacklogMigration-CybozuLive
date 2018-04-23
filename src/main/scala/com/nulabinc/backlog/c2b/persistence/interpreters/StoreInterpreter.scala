package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl.{StoreADT, WriteType}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

trait StoreInterpreter[F[_]] extends (StoreADT ~> F) {

  def run[A](prg: StoreProgram[A]): F[A]

  def createDatabase(): F[Unit]

  def getTodos: F[Observable[CybozuDBTodo]]

  def getTodo(id: Id[CybozuTodo]): F[Option[CybozuTodo]]

  def storeTodo(issue: CybozuDBTodo, writeType: WriteType): F[AnyId]

  def getTodoCount: F[Int]

  def getEvents: F[Observable[CybozuDBEvent]]

  def getEvent(id: AnyId): F[Option[CybozuEvent]]

  def storeEvent(event: CybozuDBEvent, writeType: WriteType): F[AnyId]

  def getEventCount: F[Int]

  def getForums: F[Observable[CybozuDBForum]]

  def getForum(id: AnyId): F[Option[CybozuForum]]

  def storeForum(forum: CybozuDBForum, writeType: WriteType): F[AnyId]

  def getForumCount: F[Int]
}
