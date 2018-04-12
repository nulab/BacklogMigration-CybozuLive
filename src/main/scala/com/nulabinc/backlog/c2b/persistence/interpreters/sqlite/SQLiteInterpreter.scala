package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite

import java.nio.file.Path

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters.StoreInterpreter
import monix.eval.Task
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops.AllTableOps
import monix.execution.Scheduler
import monix.reactive.Observable
import slick.jdbc.SQLiteProfile.api._

class SQLiteInterpreter(dbPath: Path)(implicit exc: Scheduler) extends StoreInterpreter[Task] {

  val allTableOps = AllTableOps()

  import allTableOps._

  private val db = Database.forURL(s"jdbc:sqlite:${dbPath.toAbsolutePath}", driver = "org.sqlite.JDBC")

  override def run[A](prg: StoreProgram[A]): Task[A] =
    prg.foldMap(this)

  override def createDatabase(): Task[Unit] = Task.deferFuture {
    db.run(createDatabaseOps)
  }

  override def getTodos: Task[Observable[CybozuDBTodo]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(todoTableOps.stream)
    )
  }

  override def getTodo(id: AnyId): Task[Option[CybozuTodo]] = Task.deferFuture {
    db.run(todoTableOps.getTodo(id))
  }

  override def storeTodo(issue: CybozuDBTodo, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(todoTableOps.write(issue, writeType))
  }

  override def getTodoCount: Task[Int] = Task.deferFuture {
    db.run(todoTableOps.count)
  }

  override def getEvents: Task[Observable[CybozuDBEvent]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(eventTableOps.stream)
    )
  }

  override def getEvent(id: AnyId): Task[Option[CybozuEvent]] = Task.deferFuture {
    db.run(eventTableOps.getEvent(id))
  }

  override def storeEvent(event: CybozuDBEvent, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(eventTableOps.write(event, writeType))
  }

  override def getEventCount: Task[AnyId] = Task.deferFuture {
    db.run(eventTableOps.count)
  }

  override def getForums: Task[Observable[CybozuDBForum]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(forumTableOps.stream)
    )
  }

  override def getForum(id: AnyId): Task[Option[CybozuForum]] = Task.deferFuture {
    db.run(forumTableOps.getForum(id))
  }

  override def storeForum(forum: CybozuDBForum, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(forumTableOps.write(forum, writeType))
  }

  override def getForumCount: Task[AnyId] = Task.deferFuture {
    db.run(forumTableOps.count)
  }

  def getCybozuUserById(id: AnyId): Task[Option[CybozuUser]] = Task.deferFuture {
    db.run(cybozuUserTableOps.select(Id[CybozuUser](id)))
  }



  override def apply[A](fa: StoreADT[A]): Task[A] = {

    import allTableOps._

    fa match {
      case Pure(a) => Task(a)
      case CreateDatabase => createDatabase()
      case GetTodos => getTodos
      case GetTodoCount => getTodoCount
      case GetTodo(id) => getTodo(id)
      case StoreTodo(issue, writeType) => storeTodo(issue, writeType)
      case GetForum(id) => getForum(id)
      case GetForumCount => getForumCount
      case GetForums => getForums
      case StoreForum(forum, writeType) => storeForum(forum, writeType)
      case GetEvent(id) => getEvent(id)
      case GetEventCount => getEventCount
      case GetEvents => getEvents
      case StoreEvent(event, writeType) => storeEvent(event, writeType)
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
      case GetCybozuUserById(id) => getCybozuUserById(id)
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
          db.stream(todoTableOps.distinctPriorities)
        )
      }
      case GetCybozuStatuses => Task.eval {
        Observable.fromReactivePublisher(
          db.stream(todoTableOps.distinctStatuses)
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
