package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

sealed trait StoreADT[A]

case class Pure[A](a: A) extends StoreADT[A]

case class GetUsers(offset: Long, size: Long) extends StoreADT[Observable[CybozuCSVUser]]
case class StoreUser(user: CybozuCSVUser) extends StoreADT[Unit]

case class GetIssues(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuCSVIssue]]
case class StoreIssue(issue: CybozuCSVIssue) extends StoreADT[Unit]

case class GetIssueComments(issue: CybozuCSVIssue) extends StoreADT[Observable[CybozuCSVComment]]
case class StoreIssueComment(issue: CybozuCSVIssue, comment: CybozuCSVComment) extends StoreADT[Unit]

case object GetPriorities extends StoreADT[Observable[CybozuCSVPriority]]
case class StorePriority(priority: CybozuCSVPriority) extends StoreADT[Unit]

case object GetStatuses extends StoreADT[Observable[CybozuCSVStatus]]
case class StoreStatus(status: CybozuCSVStatus) extends StoreADT[Unit]

case class GetEvents(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuCSVEvent]]
case class StoreEvent(event: CybozuCSVEvent) extends StoreADT[Unit]

case class GetForums(limit: Int, start: Int = 0, step: Int = 100) extends StoreADT[Observable[CybozuCSVForum]]
case class StoreForum(forum: CybozuCSVForum) extends StoreADT[Unit]