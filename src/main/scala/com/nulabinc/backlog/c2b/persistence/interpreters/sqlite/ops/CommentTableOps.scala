package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuDBComment
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOStream
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables._
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] abstract class CommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBComment, CommentTable] {

  def streamByParentId(id: AnyId): DBIOStream[CybozuDBComment] =
    tableQuery
      .filter(_.parentId === id)
      .sortBy(_.id.desc) // Comments are stored by desc. It means getting by asc.
      .result
}

private[sqlite] case class ToDoCommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBComment, ToDoCommentTable] {
  override val tableQuery = TableQuery[ToDoCommentTable]
}

private[sqlite] case class EventCommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBComment, EventCommentTable] {
  override val tableQuery = TableQuery[EventCommentTable]
}

private[sqlite] case class ForumCommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBComment, ForumCommentTable] {
  override val tableQuery = TableQuery[ForumCommentTable]
}
