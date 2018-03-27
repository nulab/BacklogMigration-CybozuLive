package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuComment
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIOStream, DBIOWrite, DBIOWrites}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CommentTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class CommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuComment, CommentTable] {

  protected val tableQuery = TableQuery[CommentTable]

  def save(comment: CybozuComment): DBIOWrite =
    tableQuery
      .returning(tableQuery.map(_.id))
      .insertOrUpdate(comment)

  def save(comments: Seq[CybozuComment]): DBIOWrites =
    DBIO.sequence(comments.map { current =>
      tableQuery.insertOrUpdate(current)
    }).map(_.sum)

  def streamByParentId(id: AnyId): DBIOStream[CybozuComment] =
    tableQuery.filter(_.parentId === id).result

}
