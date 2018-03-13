package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.persistence.dsl.{GetUsers, Pure, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import cats.Monad
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import slick.jdbc.SQLiteProfile.api._

case class SQLiteInterpreter(configPath: String) extends DBInterpreter {

  val allTableOps = AllTableOps()

  private val db = Database.forConfig(configPath)

  implicit val monad: Monad[Task] = implicitly[Monad[Task]]

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)

  override def apply[A](fa: StoreADT[A]): Task[A] = {

    import allTableOps._

    fa match {
      case Pure(a) => Task.deferFuture {
        db.run(DBIO.successful(a))
      }
      case GetUsers(offset, limit) => Task.deferFuture {
        db.run(userTableOps.selectAll(offset, limit))
      }
    }
  }

}
