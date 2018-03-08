package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import cats.Monad
import com.nulabinc.backlog.c2b.persistence.dsl.{Pure, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import slick.dbio.DBIO
import slick.jdbc.SQLiteProfile.api._

case class SQLiteInterpreter(configPath: String) extends DBInterpreter {

  private val db = Database.forConfig(configPath)

  implicit val monad: Monad[DBIO] = ???

  override def run[A](prg: StoreProgram[A]): Task[A] = ???
//    db.run(prg.foldMap(this))

  override def apply[A](fa: StoreADT[A]): DBIO[A] = ???

//  override def apply[A](fa: StoreADT[A]): DBIO[A] = fa match {
//    case Pure(a) => DBIO.successful(a)
//  }

}
