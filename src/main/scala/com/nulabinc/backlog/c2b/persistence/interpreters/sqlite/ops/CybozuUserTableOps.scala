package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuUser
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.exceptions.{SQLiteError, SQLiteException}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CybozuUserTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._



private[sqlite] case class CybozuUserTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuUser, CybozuUserTable] {

  protected val tableQuery = TableQuery[CybozuUserTable]

  def save(user: CybozuUser): DBIOWrite =
    tableQuery
      .returning(tableQuery.map(_.id))
      .insertOrUpdate(user)
      .map(optId => optId.getOrElse(throw SQLiteException(SQLiteError.IdNotGenerated)))

}
