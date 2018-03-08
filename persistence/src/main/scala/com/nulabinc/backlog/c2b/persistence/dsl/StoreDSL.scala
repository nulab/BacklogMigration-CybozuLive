package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.domains._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  def getUsers: StoreProgram[Observable[CybozuUser]] =
    Free.liftF(GetUsers)

  def storeUser(user: CybozuUser): StoreProgram[Unit] =
    Free.liftF(StoreUser(user))

  def getIssues(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuIssue]] =
    Free.liftF(GetIssues(limit, start, step))

  def storeIssue(issue: CybozuIssue): StoreProgram[Unit] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Observable[CybozuComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(issue: CybozuIssue, comment: CybozuComment): StoreProgram[Unit] =
    Free.liftF(StoreIssueComment(issue, comment))

  def getPriorities: StoreProgram[Observable[CybozuPriority]] =
    Free.liftF(GetPriorities)

  def storePriority(priority: CybozuPriority): StoreProgram[Unit] =
    Free.liftF(StorePriority(priority))

  def getStatuses: StoreProgram[Observable[CybozuStatus]] =
    Free.liftF(GetStatuses)

  def storeStatus(status: CybozuStatus): StoreProgram[Unit] =
    Free.liftF(StoreStatus(status))

  def getEvents(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuEvent]] =
    Free.liftF(GetEvents(limit, start, step))

  def storeEvent(event: CybozuEvent): StoreProgram[Unit] =
    Free.liftF(StoreEvent(event))

  def getForums(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuForum]] =
    Free.liftF(GetForums(limit, start, step))

  def storeForum(forum: CybozuForum): StoreProgram[Unit] =
    Free.liftF(StoreForum(forum))

}
