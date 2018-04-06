package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.{CybozuDBPriority, CybozuDBStatus, CybozuDBUser, Entity}
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

  implicit val statusMapper: JdbcType[CybozuDBStatus] with BaseTypedType[CybozuDBStatus] =
    MappedColumnType.base[CybozuDBStatus, String](
      src => src.value,
      dst => CybozuDBStatus(dst)
    )

  implicit val priorityMapper: JdbcType[CybozuDBPriority] with BaseTypedType[CybozuDBPriority] =
    MappedColumnType.base[CybozuDBPriority, String](
      src => src.value,
      dst => CybozuDBPriority(dst)
    )
}

private[sqlite] abstract class BaseTable[A <: Entity](tag: Tag, name: String) extends Table[A](tag, name) {

  def id: Rep[AnyId] = column[AnyId]("id", O.PrimaryKey, O.Unique, O.AutoInc)

}
