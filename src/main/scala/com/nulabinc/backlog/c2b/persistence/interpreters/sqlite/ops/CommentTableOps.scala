package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuComment
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CommentTable
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class CommentTableOps() extends BaseTableOps[CybozuComment, CommentTable] {

  protected val tableQuery = TableQuery[CommentTable]

  def save(comment: CybozuComment): DBIOWrite[CybozuComment] =
    tableQuery
      .filter(_.id === comment.id)
      .insertOrUpdate(comment)
      .transactionally

  def selectAllByParentId(id: AnyId, offset: Long, size: Long): DBIORead[Seq[CybozuComment]] =
    tableQuery.filter(_.parentId === id).drop(offset).take(size).result

}
