package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.{CybozuPriority, CybozuStatus, CybozuUser}
import com.nulabinc.backlog.c2b.persistence.datas.CybozuEntity
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

private[sqlite] abstract class BaseTable[A <: CybozuEntity](tag: Tag, name: String) extends Table[A](tag, name) {
  
  implicit val zonedDateTimeMapper: JdbcType[ZonedDateTime] with BaseTypedType[ZonedDateTime] =
    MappedColumnType.base[ZonedDateTime, Long](
      zonedDateTime => zonedDateTime.toInstant.getEpochSecond,
      epoch => ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
    )

  implicit val statusMapper: JdbcType[CybozuStatus] with BaseTypedType[CybozuStatus] =
    MappedColumnType.base[CybozuStatus, String](
      item => item.value,
      str => CybozuStatus(str)
    )

  implicit val priorityMapper: JdbcType[CybozuPriority] with BaseTypedType[CybozuPriority] =
    MappedColumnType.base[CybozuPriority, String](
      item => item.value,
      str => CybozuPriority(str)
    )

}
