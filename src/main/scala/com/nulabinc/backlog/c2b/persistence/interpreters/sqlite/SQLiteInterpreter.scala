package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.execution.Scheduler
import monix.reactive.Observable
import slick.jdbc.SQLiteProfile.api._

class SQLiteInterpreter(configPath: String)(implicit exc: Scheduler) extends DBInterpreter {

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
      case CreateDatabase => Task.deferFuture {
        val sqls = DBIO.seq(
          issueTableOps.createTable,
          commentTableOps.createTable,
          eventTableOps.createTable,
          forumTableOps.createTable,
          backlogUserTableOps.createTable,
          backlogPriorityTableOps.createTable
        )
        db.run(sqls)
      }
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
        val a = commentTableOps.save(comment)
        db.run(a)
      }
      case StoreComments(comments) => Task.deferFuture {
        db.run(commentTableOps.save(comments))
      }
      case StoreBacklogUser(user) => Task.deferFuture {
        db.run(backlogUserTableOps.save(user))
      }
      case StoreBacklogPriorities(priority) => Task.deferFuture {
        db.run(backlogPriorityTableOps.save(priority))
      }
      case WriteDBStream(stream) =>
        stream.map(_.asInstanceOf[StoreProgram[A]]).mapTask[A](run).headL
    }
  }

}
