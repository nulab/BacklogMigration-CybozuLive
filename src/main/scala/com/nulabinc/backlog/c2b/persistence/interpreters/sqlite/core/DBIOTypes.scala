package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import slick.dbio.Effect.{All, Read}
import slick.dbio.{DBIOAction, NoStream, StreamingDBIO}
import slick.sql.FixedSqlAction

// http://slick.lightbend.com/doc/3.2.0/dbio.html
object DBIOTypes {

  type DBIORead[X] = DBIOAction[X, NoStream, Read]
  type DBIOWrite = FixedSqlAction[Option[AnyId], NoStream, All]
  type DBIOWrites = DBIOAction[Int, NoStream, All]
  type DBIOStream[A] = StreamingDBIO[Seq[A], A]

}
