package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.{CybozuDBComment, Entity, Id}
import com.nulabinc.backlog.c2b.persistence.dsl.{Insert, Update, WriteType}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOStream, DBIOWrite, DBIOWrites}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.BaseTable
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

private[sqlite] trait BaseTableOps[A <: Entity, Table <: BaseTable[A]] {

  protected def tableQuery: TableQuery[Table]

  lazy val createTable = tableQuery.schema.create

  lazy val stream: DBIOStream[A] =
    tableQuery.result

  def select(id: Id[A]): DBIORead[Option[A]] =
    tableQuery.filter(_.id === id.value).result.headOption

  def write(obj: A, writeType: WriteType)(implicit exc: ExecutionContext): DBIOWrite =
    writeType match {
      case Insert =>
        (tableQuery returning tableQuery.map(_.id)) += obj
      case Update =>
        tableQuery.filter(_.id === obj.id).update(obj)
          .map(_ => obj.id)
    }

  def write(objs: Seq[A], writeType: WriteType)(implicit exc: ExecutionContext): DBIOWrites =
    DBIO.sequence(
      objs.map(write(_, writeType))
    )
}
