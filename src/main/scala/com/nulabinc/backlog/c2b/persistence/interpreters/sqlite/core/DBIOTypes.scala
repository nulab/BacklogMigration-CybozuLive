package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core

import slick.dbio.Effect.{All, Read}
import slick.dbio.{DBIOAction, NoStream}

object DBIOTypes {

  type DBIORead[X] = DBIOAction[X, NoStream, Read]
  type DBIOWrite[X] = DBIOAction[Int, NoStream, All]

}
