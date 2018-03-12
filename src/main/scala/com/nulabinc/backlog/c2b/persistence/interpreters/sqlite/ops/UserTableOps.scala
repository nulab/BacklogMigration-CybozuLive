package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuUser
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.UserTable
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class UserTableOps() extends BaseTableOps[CybozuUser, UserTable] {

  protected val tableQuery = TableQuery[UserTable]

  def save(user: CybozuUser): DBIOWrite[CybozuUser] =
    tableQuery
      .filter(_.id === user.id)
      .insertOrUpdate(user)
      .transactionally

}
