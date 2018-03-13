package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

sealed trait StoreADT[A]

case class Pure[A](a: A) extends StoreADT[A]

case class GetUsers(offset: Long, size: Long) extends StoreADT[Observable[Seq[CybozuUser]]]
case class GetUser(userId: Id[CybozuUser]) extends StoreADT[Observable[Option[CybozuUser]]]
case class StoreUser(user: CybozuUser) extends StoreADT[Unit]

case class GetIssues(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[Seq[CybozuIssue]]]
case class StoreIssue(issue: CybozuIssue) extends StoreADT[Unit]

case class GetIssueComments(issue: CybozuIssue) extends StoreADT[Observable[Seq[CybozuComment]]]
case class StoreIssueComment(issue: CybozuIssue, comment: CybozuComment) extends StoreADT[Unit]

// TODO: Add later
//case object GetPriorities extends StoreADT[Observable[Seq[CybozuPriority]]]
//case class StorePriority(priority: CybozuPriority) extends StoreADT[Unit]
//
//case object GetStatuses extends StoreADT[Observable[Seq[CybozuStatus]]]
//case class StoreStatus(status: CybozuStatus) extends StoreADT[Unit]

case class GetEvents(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[Seq[CybozuEvent]]]
case class StoreEvent(event: CybozuEvent) extends StoreADT[Unit]

case class GetForums(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[Seq[CybozuForum]]]
case class StoreForum(forum: CybozuForum) extends StoreADT[Unit]