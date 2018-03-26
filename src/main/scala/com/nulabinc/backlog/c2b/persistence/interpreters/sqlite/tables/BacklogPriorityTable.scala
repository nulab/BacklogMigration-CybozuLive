package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.BacklogPriority
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.{ProvenShape, Tag}

private[sqlite] class BacklogPriorityTable(tag: Tag) extends BaseTable[BacklogPriority](tag, "backlog_priorities") {

  def name: Rep[String] = column[String]("name")

  override def * : ProvenShape[BacklogPriority] =
    (id, name) <> (BacklogPriority.tupled, BacklogPriority.unapply)
}
