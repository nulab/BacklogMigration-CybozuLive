package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.persistence.dsl.{Pure, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import cats.Monad
import slick.jdbc.SQLiteProfile.api._

case class SQLiteInterpreter(configPath: String) extends DBInterpreter {

  private val db = Database.forConfig(configPath)

  implicit val monad: Monad[Task] = implicitly[Monad[Task]]

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)
  
  override def apply[A](fa: StoreADT[A]): Task[A] = ???

//  override def apply[A](fa: StoreADT[A]): DBIO[A] = fa match {
//    case Pure(a) => DBIO.successful(a)
//  }

}
