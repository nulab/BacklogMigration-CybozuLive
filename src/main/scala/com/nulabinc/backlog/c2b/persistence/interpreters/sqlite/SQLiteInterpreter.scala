package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import com.nulabinc.backlog.c2b.datas.{CybozuDBUser, Id}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StoreInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.execution.Scheduler
import monix.reactive.Observable
import slick.jdbc.SQLiteProfile.api._

class SQLiteInterpreter(configPath: String)(implicit exc: Scheduler) extends StoreInterpreter {

  val allTableOps = AllTableOps()

  import allTableOps._

  private val db = Database.forConfig(configPath)

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)

  def getCybozuUserById(id: AnyId): Task[Option[CybozuDBUser]] = Task.deferFuture {
    db.run(cybozuUserTableOps.select(Id[CybozuDBUser](id)))
  }

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
      case GetTodos => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(issueTableOps.stream)
        )
      }
      case StoreTodo(issue, writeType) => Task.deferFuture {
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
      case GetComments(issue) => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(commentTableOps.streamByParentId(issue.id))
        )
      }
      case StoreComment(comment, writeType) => Task.deferFuture {
        db.run(commentTableOps.write(comment, writeType))
      }
      case StoreComments(comments, writeType) => Task.deferFuture {
        db.run(commentTableOps.write(comments, writeType))
      }
      case StoreTodoAssignees(issueId, assigneeIds) => Task.deferFuture {
        db.run(cybozuIssueUserTableOps.write(issueId, assigneeIds))
      }
      case GetCybozuUsers => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(cybozuUserTableOps.stream)
        )
      }
      case GetCybozuUserById(id) =>
        getCybozuUserById(id)
      case GetCybozuUserBykey(key) => Task.deferFuture {
        db.run(cybozuUserTableOps.findByKey(key))
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
      case GetCybozuPriorities => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(issueTableOps.distinctPriorities)
        )
      }
      case GetCybozuStatuses => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(issueTableOps.distinctStatuses)
        )
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
