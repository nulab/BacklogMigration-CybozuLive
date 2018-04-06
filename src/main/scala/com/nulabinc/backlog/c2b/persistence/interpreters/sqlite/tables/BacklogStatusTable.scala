package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.BacklogStatus
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

private[sqlite] class BacklogStatusTable(tag: Tag) extends BaseTable[BacklogStatus](tag, "backlog_statuses") {

  def name: Rep[String] = column[String]("name")

  override def * : ProvenShape[BacklogStatus] =
    (id, name) <> (BacklogStatus.tupled, BacklogStatus.unapply)
}
