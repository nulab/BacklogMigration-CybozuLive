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

  lazy val getIssues: StoreProgram[Observable[CybozuIssue]] =
    Free.liftF(GetIssues)

  lazy val getEvents: StoreProgram[Observable[CybozuEvent]] =
    Free.liftF(GetEvents)

  lazy val getForums: StoreProgram[Observable[CybozuForum]] =
    Free.liftF(GetForums)

  def storeIssue(issue: CybozuIssue): StoreProgram[AnyId] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Observable[CybozuComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(comment: CybozuComment): StoreProgram[AnyId] =
    Free.liftF(StoreComment(comment))

  def storeComments(comments: Seq[CybozuComment]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreComments(comments))

//  def getPriorities: StoreProgram[Observable[CybozuCSVPriority]] =
//    Free.liftF(GetPriorities)
//
//  def storePriority(priority: CybozuCSVPriority): StoreProgram[Unit] =
//    Free.liftF(StorePriority(priority))
//
//  def getStatuses: StoreProgram[Observable[CybozuCSVStatus]] =
//    Free.liftF(GetStatuses)
//
//  def storeStatus(status: CybozuCSVStatus): StoreProgram[Unit] =
//    Free.liftF(StoreStatus(status))



  def storeEvent(event: CybozuEvent): StoreProgram[AnyId] =
    Free.liftF(StoreEvent(event))

  def storeForum(forum: CybozuForum): StoreProgram[AnyId] =
    Free.liftF(StoreForum(forum))

  def writeDBStream[A](stream: Observable[StoreProgram[A]]): StoreProgram[AnyId] =
    Free.liftF[StoreADT, AnyId](WriteDBStream(stream))

  def storeBacklogUser(user: BacklogUser): StoreProgram[AnyId] =
    Free.liftF(StoreBacklogUser(user))

  def storeBacklogPriorities(priorities: Seq[BacklogPriority]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogPriorities(priorities))

  def storeBacklogStatuses(statuses: Seq[BacklogStatus]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogStatuses(statuses))

}
