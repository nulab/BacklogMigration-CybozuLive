package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  def getUsers(offset: Long, size: Long): StoreProgram[Observable[CybozuCSVUser]] =
    Free.liftF(GetUsers(offset, size))

  def storeUser(user: CybozuCSVUser): StoreProgram[Unit] =
    Free.liftF(StoreUser(user))

  def getIssues(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuCSVIssue]] =
    Free.liftF(GetIssues(limit, start, step))

  def storeIssue(issue: CybozuCSVIssue): StoreProgram[Unit] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuCSVIssue): StoreProgram[Observable[CybozuCSVComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(issue: CybozuCSVIssue, comment: CybozuCSVComment): StoreProgram[Unit] =
    Free.liftF(StoreIssueComment(issue, comment))

  def getPriorities: StoreProgram[Observable[CybozuCSVPriority]] =
    Free.liftF(GetPriorities)

  def storePriority(priority: CybozuCSVPriority): StoreProgram[Unit] =
    Free.liftF(StorePriority(priority))

  def getStatuses: StoreProgram[Observable[CybozuCSVStatus]] =
    Free.liftF(GetStatuses)

  def storeStatus(status: CybozuCSVStatus): StoreProgram[Unit] =
    Free.liftF(StoreStatus(status))

  def getEvents(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuCSVEvent]] =
    Free.liftF(GetEvents(limit, start, step))

  def storeEvent(event: CybozuCSVEvent): StoreProgram[Unit] =
    Free.liftF(StoreEvent(event))

  def getForums(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[CybozuCSVForum]] =
    Free.liftF(GetForums(limit, start, step))

  def storeForum(forum: CybozuCSVForum): StoreProgram[Unit] =
    Free.liftF(StoreForum(forum))

}
