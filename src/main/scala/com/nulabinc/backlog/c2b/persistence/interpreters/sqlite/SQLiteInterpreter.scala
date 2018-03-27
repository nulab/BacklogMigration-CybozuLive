package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.interpreters.ConsumeStream
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.execution.Scheduler
import monix.reactive.{Consumer, Observable}
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
          backlogPriorityTableOps.createTable,
          backlogStatusTableOps.createTable,
          cybozuUserTableOps.createTable,
          cybozuIssueUserTableOps.createTable
        )
        db.run(sqls)
      }
      case GetIssues => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(issueTableOps.stream)
        )
      }
      case StoreIssue(issue, writeType) => Task.deferFuture {
        db.run(issueTableOps.write(issue, writeType))
      }
      case GetForums => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(forumTableOps.stream)
        )
      }
      case StoreForum(forum, writeType) => Task.deferFuture {
        db.run(forumTableOps.write(forum, writeType))
      }
      case GetEvents => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(eventTableOps.stream)
        )
      }
      case StoreEvent(event, writeType) => Task.deferFuture {
        db.run(eventTableOps.write(event, writeType))
      }
      case GetIssueComments(issue) => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(commentTableOps.streamByParentId(issue.id))
        )
      }
      case StoreComment(comment, writeType) => Task.deferFuture {
        val a = commentTableOps.write(comment, writeType)
        db.run(a)
      }
      case StoreComments(comments, writeType) => Task.deferFuture {
        db.run(commentTableOps.write(comments, writeType))
      }
      case StoreCybozuUser(user, writeType) => Task.deferFuture {
        db.run(cybozuUserTableOps.write(user, writeType))
      }

      case StoreBacklogUser(user, writeType) => Task.deferFuture {
        db.run(backlogUserTableOps.write(user, writeType))
      }
      case GetBacklogUsers => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(backlogUserTableOps.stream)
        )
      }
      case StoreBacklogPriorities(priority, writeType) => Task.deferFuture {
        db.run(backlogPriorityTableOps.write(priority, writeType))
      }
      case GetBacklogPriorities => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(backlogPriorityTableOps.stream)
        )
      }
      case StoreBacklogStatuses(statuses, writeType) => Task.deferFuture {
        db.run(backlogStatusTableOps.write(statuses, writeType))
      }
      case GetBacklogStatuses => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(backlogStatusTableOps.stream)
        )
      }
      case GetCybozuPriorities => Task.deferFuture {
        db.run(issueTableOps.distinctPriorities)
      }
      case WriteDBStream(stream) =>
        Task.deferFuture {
          stream.map(_.asInstanceOf[StoreProgram[A]]).mapTask { prg =>
            prg.foldMap(this)
          }.foreach(_ => ())
        }
    }
  }

}
