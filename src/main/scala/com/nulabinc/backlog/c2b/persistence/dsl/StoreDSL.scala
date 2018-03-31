package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def empty[A]: StoreProgram[Option[A]] =
    Free.liftF(Pure(None))

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  lazy val createDatabase: StoreProgram[Unit] =
    Free.liftF(CreateDatabase)

  lazy val getTodos: StoreProgram[Observable[CybozuTodo]] =
    Free.liftF(GetTodos)

  lazy val getEvents: StoreProgram[Observable[CybozuEvent]] =
    Free.liftF(GetEvents)

  lazy val getForums: StoreProgram[Observable[CybozuForum]] =
    Free.liftF(GetForums)

  lazy val getCybozuPriorities: StoreProgram[Observable[CybozuPriority]] =
    Free.liftF(GetCybozuPriorities)

  lazy val getCybozuStatuses: StoreProgram[Observable[CybozuStatus]] =
    Free.liftF(GetCybozuStatuses)

  lazy val getBacklogUsers: StoreProgram[Observable[BacklogUser]] =
    Free.liftF(GetBacklogUsers)

  lazy val getBacklogPriorities: StoreProgram[Observable[BacklogPriority]] =
    Free.liftF(GetBacklogPriorities)

  lazy val getBacklogStatuses: StoreProgram[Observable[BacklogStatus]] =
    Free.liftF(GetBacklogStatuses)

  def storeTodo(todo: CybozuTodo): StoreProgram[AnyId] =
    Free.liftF(StoreTodo(todo))

  def getTodoComments(todo: CybozuTodo): StoreProgram[Observable[CybozuComment]] =
    Free.liftF(GetComments(todo))

  def storeTodoComment(comment: CybozuComment): StoreProgram[AnyId] =
    Free.liftF(StoreComment(comment))

  def storeTodoComments(comments: Seq[CybozuComment]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreComments(comments))

  def storeTodoAssignees(todoId: AnyId, assigneeIds: Seq[AnyId]): StoreProgram[Int] =
    Free.liftF(StoreTodoAssignees(todoId, assigneeIds))

  def storeEvent(event: CybozuEvent): StoreProgram[AnyId] =
    Free.liftF(StoreEvent(event))

  def storeForum(forum: CybozuForum): StoreProgram[AnyId] =
    Free.liftF(StoreForum(forum))

  //
  // Cybozu user
  //
  lazy val getCybozuUsers: StoreProgram[Observable[CybozuUser]] =
    Free.liftF(GetCybozuUsers)

  def getCybozuUserById(id: AnyId): StoreProgram[Option[CybozuUser]] =
    Free.liftF(GetCybozuUserById(id))

  def getCybozuUserByKey(key: String): StoreProgram[Option[CybozuUser]] =
    Free.liftF(GetCybozuUserBykey(key))

  def storeCybozuUser(user: CybozuUser, writeType: WriteType): StoreProgram[AnyId] =
    Free.liftF(StoreCybozuUser(user, writeType))

  //
  // Backlog user
  //
  def storeBacklogUser(user: BacklogUser): StoreProgram[AnyId] =
    Free.liftF(StoreBacklogUser(user))

  def storeBacklogPriorities(priorities: Seq[BacklogPriority]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogPriorities(priorities))

  def storeBacklogStatuses(statuses: Seq[BacklogStatus]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogStatuses(statuses))

  def writeDBStream[A](stream: Observable[StoreProgram[A]]): StoreProgram[Unit] =
    Free.liftF[StoreADT, Unit](WriteDBStream(stream))
}
