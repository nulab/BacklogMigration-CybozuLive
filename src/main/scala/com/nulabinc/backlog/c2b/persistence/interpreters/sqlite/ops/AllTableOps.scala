package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import monix.execution.Scheduler

private[sqlite] case class AllTableOps()(implicit exc: Scheduler) {
  val commentTableOps = CommentTableOps()
  val eventTableOps = EventTableOps()
  val forumTableOps = ForumTableOps()
  val issueTableOps = IssueTableOps()
  val backlogUserTableOps = BacklogUserTableOps()
  val backlogPriorityTableOps = BacklogPriorityTableOps()
  val backlogStatusTableOps = BacklogStatusTableOps()
}
