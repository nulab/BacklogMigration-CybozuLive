package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.domains._
import monix.reactive.Observable

sealed trait StoreADT[A]

case class Pure[A](a: A) extends StoreADT[A]

case object GetUsers extends StoreADT[Observable[CybozuUser]]
case class StoreUser(user: CybozuUser) extends StoreADT[Unit]

case class GetIssues(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuIssue]]
case class StoreIssue(issue: CybozuIssue) extends StoreADT[Unit]

case class GetIssueComments(issue: CybozuIssue) extends StoreADT[Observable[CybozuComment]]
case class StoreIssueComment(issue: CybozuIssue, comment: CybozuComment) extends StoreADT[Unit]

case object GetPriorities extends StoreADT[Observable[CybozuPriority]]
case class StorePriority(priority: CybozuPriority) extends StoreADT[Unit]

case object GetStatuses extends StoreADT[Observable[CybozuStatus]]
case class StoreStatus(status: CybozuStatus) extends StoreADT[Unit]

case class GetEvents(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuEvent]]
case class StoreEvent(event: CybozuEvent) extends StoreADT[Unit]

case class GetForums(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuForum]]
case class StoreForum(forum: CybozuForum) extends StoreADT[Unit]