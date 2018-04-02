package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuDBUser
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.exceptions.{SQLiteError, SQLiteException}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CybozuUserTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._



private[sqlite] case class CybozuUserTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBUser, CybozuUserTable] {

  protected val tableQuery = TableQuery[CybozuUserTable]

  def save(user: CybozuDBUser): DBIOWrite =
    tableQuery
      .returning(tableQuery.map(_.id))
      .insertOrUpdate(user)
      .map(optId => optId.getOrElse(throw SQLiteException(SQLiteError.IdNotGenerated)))

  def findByKey(key: String): DBIORead[Option[CybozuDBUser]] =
    tableQuery
    .filter(_.userId === key)
    .result
    .headOption

  def select(ids: Seq[AnyId]): DBIO[Seq[CybozuDBUser]] =
    tableQuery.filter(_.id.inSet(ids)).result

}
