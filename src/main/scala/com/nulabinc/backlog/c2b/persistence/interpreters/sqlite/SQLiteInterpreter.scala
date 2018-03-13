package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.persistence.dsl.{GetUsers, StoreADT}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.reactive.Observable
import slick.jdbc.SQLiteProfile.api._

case class SQLiteInterpreter(configPath: String) extends DBInterpreter {

  val allTableOps = AllTableOps()

  private val db = Database.forConfig(configPath)

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)

  // https://monix.io/docs/2x/eval/task.html
  // https://monix.io/docs/2x/reactive/observable.html
  override def apply[A](fa: StoreADT[A]): Task[A] = {
    import allTableOps._

    fa match {
      case GetUsers => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(userTableOps.stream)
        )
      }
    }
  }

}
