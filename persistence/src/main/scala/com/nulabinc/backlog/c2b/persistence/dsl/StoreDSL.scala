package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.domains._

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  def getUsers: StoreProgram[Seq[CybozuUser]] =
    Free.liftF(GetUsers)

  def storeUser(user: CybozuUser): StoreProgram[Unit] =
    Free.liftF(StoreUser(user))

  def getIssues(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Seq[CybozuUser]] =
    Free.liftF(GetUsers(limit, start, step))

  def storeIssue(issue: CybozuIssue): StoreProgram[Unit] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Seq[CybozuComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(issue: CybozuIssue, comment: CybozuComment): StoreProgram[Unit] =
    Free.liftF(StoreIssueComment(issue, comment))

  def getPriorities: StoreProgram[Seq[CybozuPriority]] =
    Free.liftF(GetPriorities)

  def storePriority(priority: CybozuPriority): StoreProgram[Unit] =
    Free.liftF(StorePriority(priority))

  def getStatuses: StoreProgram[Seq[CybozuStatus]] =
    Free.liftF(GetStatuses)

  def storeStatus(status: CybozuStatus): StoreProgram[Unit] =
    Free.liftF(StoreStatuse(status))

  def getEvents(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Seq[CybozuEvent]] =
    Free.liftF(GetEvents(limit, start, step))

  def storeEvent(event: CybozuEvent): StoreProgram[Unit] =
    Free.liftF(StoreEvent(event))

  def getForums(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Seq[CybozuForum]] =
    Free.liftF(GetForums(limit, start, step))

  def storeForum(forum: CybozuForum): StoreProgram[Unit] =
    Free.liftF(StoreForum(forum))

}
