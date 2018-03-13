package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

//  def pure[A](a: A): StoreProgram[A] =
//    Free.liftF(Pure(a))

  lazy val getUsers: StoreProgram[Observable[CybozuUser]] =
    Free.liftF(GetUsers)

  lazy val getIssues: StoreProgram[Observable[CybozuIssue]] =
    Free.liftF(GetIssues)

  lazy val getEvents: StoreProgram[Observable[CybozuEvent]] =
    Free.liftF(GetEvents)

  lazy val getForums: StoreProgram[Observable[CybozuForum]] =
    Free.liftF(GetForums)

  def getUser(userId: Id[CybozuUser]): StoreProgram[Option[CybozuUser]] =
    Free.liftF(GetUser(userId))

  def storeUser(user: CybozuUser): StoreProgram[AnyId] =
    Free.liftF(StoreUser(user))

  def storeIssue(issue: CybozuIssue): StoreProgram[AnyId] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Observable[CybozuComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(comment: CybozuComment): StoreProgram[AnyId] =
    Free.liftF(StoreComment(comment))

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

}
