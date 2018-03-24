package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

private[sqlite] case class AllTableOps() {
  val commentTableOps = CommentTableOps()
  val eventTableOps = EventTableOps()
  val forumTableOps = ForumTableOps()
  val issueTableOps = IssueTableOps()
//  val userTableOps = UserTableOps()
}
