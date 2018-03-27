package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

sealed trait StoreADT[A]

case class Pure[A](a: A) extends StoreADT[A]

case object CreateDatabase extends StoreADT[Unit]

case object GetIssues extends StoreADT[Observable[CybozuIssue]]
case class StoreIssue(issue: CybozuIssue) extends StoreADT[Option[AnyId]]

case class GetIssueComments(issue: CybozuIssue) extends StoreADT[Observable[CybozuComment]]
case class StoreComment(comment: CybozuComment) extends StoreADT[Option[AnyId]]
case class StoreComments(comments: Seq[CybozuComment]) extends StoreADT[Int]

case object GetEvents extends StoreADT[Observable[CybozuEvent]]
case class StoreEvent(event: CybozuEvent) extends StoreADT[Option[AnyId]]

case object GetForums extends StoreADT[Observable[CybozuForum]]
case class StoreForum(forum: CybozuForum) extends StoreADT[Option[AnyId]]

case object GetCybozuUsers extends StoreADT[Observable[CybozuUser]]
case class StoreCybozuUser(user: CybozuUser) extends StoreADT[Option[AnyId]]

case object GetCybozuPriorities extends StoreADT[Seq[CybozuPriority]]

case class WriteDBStream[A](stream: Observable[StoreProgram[A]]) extends StoreADT[A]

case object GetBacklogUsers extends StoreADT[Observable[BacklogUser]]
case class StoreBacklogUser(user: BacklogUser) extends StoreADT[Option[AnyId]]

case object GetBacklogPriorities extends StoreADT[Observable[BacklogPriority]]
case class StoreBacklogPriorities(priorities: Seq[BacklogPriority]) extends StoreADT[Int]

case object GetBacklogStatuses extends StoreADT[Observable[BacklogStatus]]
case class StoreBacklogStatuses(statuses: Seq[BacklogStatus]) extends StoreADT[Int]
