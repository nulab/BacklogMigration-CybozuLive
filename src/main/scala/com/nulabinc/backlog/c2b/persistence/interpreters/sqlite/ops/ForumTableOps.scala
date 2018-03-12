package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuForum
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.ForumTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class ForumTableOps() extends BaseTableOps[DBCybozuForum, ForumTable] {

  protected def tableQuery = TableQuery[ForumTable]

  def select(id: String): DBIORead[Option[DBCybozuForum]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def save(forum: DBCybozuForum): DBIOWrite[DBCybozuForum] =
    tableQuery
      .filter(_.id === forum.id)
      .insertOrUpdate(forum)
      .transactionally

}
