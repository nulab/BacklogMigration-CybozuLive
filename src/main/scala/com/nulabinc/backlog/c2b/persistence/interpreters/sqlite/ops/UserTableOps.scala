package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.persistence.datas.DBCybozuUser
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOWrite}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.UserTable
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class UserTableOps() extends BaseTableOps[DBCybozuUser, UserTable] {

  protected val tableQuery = TableQuery[UserTable]

  def select(id: String): DBIORead[Option[DBCybozuUser]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def save(user: DBCybozuUser): DBIOWrite[DBCybozuUser] =
    tableQuery
      .filter(_.id === user.id)
      .insertOrUpdate(user)
      .transactionally

}
