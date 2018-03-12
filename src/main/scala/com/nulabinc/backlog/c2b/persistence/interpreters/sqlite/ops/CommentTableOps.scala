package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuComment
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CommentTable
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class CommentTableOps() extends BaseTableOps[DBCybozuComment, CommentTable] {

  protected val tableQuery = TableQuery[CommentTable]

  def select(id: Long): DBIORead[Option[DBCybozuComment]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def save(comment: DBCybozuComment): DBIOWrite[DBCybozuComment] =
    tableQuery
      .filter(_.id === comment.id)
      .insertOrUpdate(comment)
      .transactionally

}
