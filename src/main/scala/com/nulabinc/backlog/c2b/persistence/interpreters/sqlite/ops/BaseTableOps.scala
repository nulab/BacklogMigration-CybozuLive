package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas.Entity
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOStream}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.BaseTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] trait BaseTableOps[A <: Entity, Table <: BaseTable[A]] {

  protected def tableQuery: TableQuery[Table]

  lazy val stream: DBIOStream[A] =
    tableQuery.result

  def select(id: AnyId): DBIORead[Option[A]] =
    tableQuery.filter(_.id === id.value).result.headOption
  
}
