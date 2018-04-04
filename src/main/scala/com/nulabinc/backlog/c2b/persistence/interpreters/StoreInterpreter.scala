package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.datas.{CybozuForum, CybozuTodo}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl.StoreADT
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram

trait StoreInterpreter[F[_]] extends (StoreADT ~> F) {

  def run[A](prg: StoreProgram[A]): F[A]

  def getTodo(id: AnyId): F[Option[CybozuTodo]]
  
  def getForum(id: AnyId): F[Option[CybozuForum]]
}
