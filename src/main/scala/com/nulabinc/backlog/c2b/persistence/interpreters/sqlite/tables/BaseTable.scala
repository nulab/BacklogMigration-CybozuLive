package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.{CybozuPriority, CybozuStatus, CybozuDBUser, Entity}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

object JdbcMapper {
  implicit val zonedDateTimeMapper: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[DateTime, Long](
      zonedDateTime => zonedDateTime.toInstant.getEpochSecond,
      epoch => ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
    )

  implicit val statusMapper: JdbcType[CybozuStatus] with BaseTypedType[CybozuStatus] =
    MappedColumnType.base[CybozuStatus, String](
      src => src.value,
      dst => CybozuStatus(dst)
    )

  implicit val priorityMapper: JdbcType[CybozuPriority] with BaseTypedType[CybozuPriority] =
    MappedColumnType.base[CybozuPriority, String](
      src => src.value,
      dst => CybozuPriority(dst)
    )
}

private[sqlite] abstract class BaseTable[A <: Entity](tag: Tag, name: String) extends Table[A](tag, name) {

  def id: Rep[AnyId] = column[AnyId]("id", O.PrimaryKey, O.Unique, O.AutoInc)

}
