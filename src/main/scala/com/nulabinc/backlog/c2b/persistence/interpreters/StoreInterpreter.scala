package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.datas.{CybozuEvent, CybozuForum, CybozuTodo}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl.StoreADT
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram

trait StoreInterpreter[F[_]] extends (StoreADT ~> F) {

  def run[A](prg: StoreProgram[A]): F[A]

  def createDatabase: F[Unit]

  def getTodo(id: AnyId): F[Option[CybozuTodo]]

  def getTodoCount: F[Int]

  def getEvent(id: AnyId): F[Option[CybozuEvent]]

  def getEventCount: F[Int]
  
  def getForum(id: AnyId): F[Option[CybozuForum]]

  def getForumCount: F[Int]
}
