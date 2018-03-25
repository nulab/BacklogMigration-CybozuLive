package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

sealed trait StoreADT[A]

case class Pure[A](a: A) extends StoreADT[A]

case object CreateDatabase extends StoreADT[Unit]

case object GetIssues extends StoreADT[Observable[CybozuIssue]]
case class StoreIssue(issue: CybozuIssue) extends StoreADT[AnyId]

case class GetIssueComments(issue: CybozuIssue) extends StoreADT[Observable[CybozuComment]]
case class StoreComment(comment: CybozuComment) extends StoreADT[AnyId]
case class StoreComments(comments: Seq[CybozuComment]) extends StoreADT[Seq[AnyId]]

// TODO: Add later
//case object GetPriorities extends StoreADT[Observable[Seq[CybozuPriority]]]
//case class StorePriority(priority: CybozuPriority) extends StoreADT[Unit]
//
//case object GetStatuses extends StoreADT[Observable[Seq[CybozuStatus]]]
//case class StoreStatus(status: CybozuStatus) extends StoreADT[Unit]

case object GetEvents extends StoreADT[Observable[CybozuEvent]]
case class StoreEvent(event: CybozuEvent) extends StoreADT[AnyId]

case object GetForums extends StoreADT[Observable[CybozuForum]]
case class StoreForum(forum: CybozuForum) extends StoreADT[AnyId]

case class WriteDBStream[A](stream: Observable[StoreProgram[A]]) extends StoreADT[AnyId]
