package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.c2b.persistence.datas.Entity
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._

private[sqlite] abstract class BaseTable[A <: Entity](tag: Tag, name: String) extends Table[A](tag, name) {

  implicit val zonedDateTimeMapper: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[DateTime, Long](
      zonedDateTime => zonedDateTime.toInstant.getEpochSecond,
      epoch => ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
    )

  def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Unique)
}
