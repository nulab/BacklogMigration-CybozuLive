package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.reactive.Observable
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future

class SQLiteInterpreter(configPath: String) extends DBInterpreter {

  val allTableOps = AllTableOps()

  private val db = Database.forConfig(configPath)

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)

  // https://monix.io/docs/2x/eval/task.html
  // https://monix.io/docs/2x/reactive/observable.html
  override def apply[A](fa: StoreADT[A]): Task[A] = {

    import allTableOps._

    fa match {
      case Pure(a) =>
        Task(a)
      case GetIssues => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(issueTableOps.stream)
        )
      }
      case StoreIssue(issue) => Task.deferFuture {
        db.run(issueTableOps.save(issue))
      }
      case GetForums => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(forumTableOps.stream)
        )
      }
      case StoreForum(forum) => Task.deferFuture {
        db.run(forumTableOps.save(forum))
      }
      case GetEvents => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(eventTableOps.stream)
        )
      }
      case StoreEvent(event) => Task.deferFuture {
        db.run(eventTableOps.save(event))
      }
      case GetIssueComments(issue) => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(commentTableOps.streamByParentId(issue.id))
        )
      }
      case StoreComment(comment) => Task.deferFuture {
        db.run(commentTableOps.save(comment))
      }
    }
  }

}
