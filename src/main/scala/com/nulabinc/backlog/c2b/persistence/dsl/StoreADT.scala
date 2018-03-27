package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

sealed trait StoreADT[A]

sealed trait WriteType
case object Insert extends WriteType
case object Update extends WriteType

case class Pure[A](a: A) extends StoreADT[A]

case object CreateDatabase extends StoreADT[Unit]

case object GetIssues extends StoreADT[Observable[CybozuIssue]]
case class StoreIssue(issue: CybozuIssue, writeType: WriteType = Insert) extends StoreADT[AnyId]

case class GetIssueComments(issue: CybozuIssue) extends StoreADT[Observable[CybozuComment]]
case class StoreComment(comment: CybozuComment, writeType: WriteType = Insert) extends StoreADT[AnyId]
case class StoreComments(comments: Seq[CybozuComment], writeType: WriteType = Insert) extends StoreADT[Seq[AnyId]]

case object GetEvents extends StoreADT[Observable[CybozuEvent]]
case class StoreEvent(event: CybozuEvent, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetForums extends StoreADT[Observable[CybozuForum]]
case class StoreForum(forum: CybozuForum, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetCybozuUsers extends StoreADT[Observable[CybozuUser]]
case class StoreCybozuUser(user: CybozuUser, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetCybozuPriorities extends StoreADT[Seq[CybozuPriority]]

case class WriteDBStream[A](stream: Observable[StoreProgram[A]]) extends StoreADT[Unit]

case object GetBacklogUsers extends StoreADT[Observable[BacklogUser]]
case class StoreBacklogUser(user: BacklogUser, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetBacklogPriorities extends StoreADT[Observable[BacklogPriority]]
case class StoreBacklogPriorities(priorities: Seq[BacklogPriority], writeType: WriteType = Insert)
  extends StoreADT[Seq[AnyId]]

case object GetBacklogStatuses extends StoreADT[Observable[BacklogStatus]]
case class StoreBacklogStatuses(statuses: Seq[BacklogStatus], writeType: WriteType = Insert)
  extends StoreADT[Seq[AnyId]]
