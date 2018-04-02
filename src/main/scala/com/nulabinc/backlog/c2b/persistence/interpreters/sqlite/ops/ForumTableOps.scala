package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuDBForum
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOWrite
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.ForumTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class ForumTableOps() extends BaseTableOps[CybozuDBForum, ForumTable] {

  protected def tableQuery = TableQuery[ForumTable]

}
