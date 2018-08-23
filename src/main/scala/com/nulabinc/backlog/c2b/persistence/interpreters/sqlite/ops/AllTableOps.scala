package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class AllTableOps()(implicit exc: Scheduler) {
  val todoCommentTableOps = ToDoCommentTableOps()
  val eventCommentTableOps = EventCommentTableOps()
  val forumCommentTableOps = ForumCommentTableOps()
  val chatCommentTableOps = ChatCommentTableOps()
  val eventTableOps = EventTableOps()
  val forumTableOps = ForumTableOps()
  val todoTableOps = TodoTableOps()
  val chatTableOps = ChatTableOps()
  val backlogUserTableOps = BacklogUserTableOps()
  val backlogPriorityTableOps = BacklogPriorityTableOps()
  val backlogStatusTableOps = BacklogStatusTableOps()
  val cybozuUserTableOps = CybozuUserTableOps()
  val cybozuIssueUserTableOps = CybozuIssueUserTableOps()

  val createDatabaseOps =
    DBIO.seq(
      todoTableOps.createTable,
      todoCommentTableOps.createTable,
      eventTableOps.createTable,
      eventCommentTableOps.createTable,
      forumTableOps.createTable,
      forumCommentTableOps.createTable,
      chatTableOps.createTable,
      chatCommentTableOps.createTable,
      backlogUserTableOps.createTable,
      backlogPriorityTableOps.createTable,
      backlogStatusTableOps.createTable,
      cybozuUserTableOps.createTable,
      cybozuIssueUserTableOps.createTable
    )
}
