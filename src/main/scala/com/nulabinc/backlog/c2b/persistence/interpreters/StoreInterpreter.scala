package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

trait StoreInterpreter[F[_]] extends (StoreADT ~> F) {

  def run[A](prg: StoreProgram[A]): F[A]

  def pure[A](a: A): F[A]

  def createDatabase(): F[Unit]

  def getTodos: F[Observable[CybozuDBTodo]]

  def getTodo(id: Id[CybozuTodo]): F[Option[CybozuTodo]]

  def storeTodo(issue: CybozuDBTodo, writeType: WriteType): F[AnyId]

  def getTodoCount: F[Int]

  def storeTodoAssignees(todoId: AnyId, assigneeIds: Seq[AnyId]): F[Int]

  def getEvents: F[Observable[CybozuDBEvent]]

  def getEvent(id: AnyId): F[Option[CybozuEvent]]

  def storeEvent(event: CybozuDBEvent, writeType: WriteType): F[AnyId]

  def getEventCount: F[Int]

  def getForums: F[Observable[CybozuDBForum]]

  def getForum(id: AnyId): F[Option[CybozuForum]]

  def storeForum(forum: CybozuDBForum, writeType: WriteType): F[AnyId]

  def getForumCount: F[Int]

  def storeComment(comment: CybozuDBComment, commentType: CommentType, writeType: WriteType): F[AnyId]

  def storeComments(comments: Seq[CybozuDBComment], commentType: CommentType, writeType: WriteType): F[Seq[AnyId]]

  def getCybozuUserById(id: AnyId): F[Option[CybozuUser]]

  def getCybozuUsers(): F[Observable[CybozuUser]]

  def getCybozuUserBykey(key: String): F[Option[CybozuUser]]

  def storeCybozuUser(user: CybozuUser, writeType: WriteType = Insert): F[AnyId]

  def getBacklogUsers(): F[Observable[BacklogUser]]

  def storeBacklogUser(user: BacklogUser, writeType: WriteType = Insert): F[AnyId]

  def getBacklogPriorities(): F[Observable[BacklogPriority]]

  def storeBacklogPriorities(priorities: Seq[BacklogPriority], writeType: WriteType = Insert): F[Seq[AnyId]]

  def getBacklogStatuses(): F[Observable[BacklogStatus]]

  def storeBacklogStatuses(statuses: Seq[BacklogStatus], writeType: WriteType = Insert): F[Seq[AnyId]]

  def getCybozuPriorities(): F[Observable[CybozuPriority]]

  def getCybozuStatuses(): F[Observable[CybozuStatus]]

  def writeDBStream[A](stream: Observable[StoreProgram[A]]): F[Unit]

  override def apply[A](fa: StoreADT[A]): F[A] = fa match {
    case Pure(a) => pure(a)
    case CreateDatabase => createDatabase()
    case GetTodos => getTodos
    case GetTodoCount => getTodoCount
    case GetTodo(id) => getTodo(id)
    case StoreTodo(issue, writeType) => storeTodo(issue, writeType)
    case GetForum(id) => getForum(id)
    case GetForumCount => getForumCount
    case GetForums => getForums
    case StoreForum(forum, writeType) => storeForum(forum, writeType)
    case GetEvent(id) => getEvent(id)
    case GetEventCount => getEventCount
    case GetEvents => getEvents
    case StoreEvent(event, writeType) => storeEvent(event, writeType)
    case StoreComment(comment, commentType, writeType) => storeComment(comment, commentType, writeType)
    case StoreComments(comments, commentType, writeType) => storeComments(comments, commentType, writeType)
    case StoreTodoAssignees(todoId, assigneeIds) => storeTodoAssignees(todoId, assigneeIds)
    case GetCybozuUsers => getCybozuUsers
    case GetCybozuUserById(id) => getCybozuUserById(id)
    case GetCybozuUserBykey(key) => getCybozuUserBykey(key)
    case StoreCybozuUser(user, writeType) => storeCybozuUser(user, writeType)
    case StoreBacklogUser(user, writeType) => storeBacklogUser(user, writeType)
    case GetBacklogUsers => getBacklogUsers()
    case StoreBacklogPriorities(priorities, writeType) => storeBacklogPriorities(priorities, writeType)
    case GetBacklogPriorities => getBacklogPriorities()
    case StoreBacklogStatuses(statuses, writeType) => storeBacklogStatuses(statuses, writeType)
    case GetBacklogStatuses => getBacklogStatuses()
    case GetCybozuPriorities => getCybozuPriorities()
    case GetCybozuStatuses => getCybozuStatuses()
    case WriteDBStream(stream) => writeDBStream(stream)
  }
}
