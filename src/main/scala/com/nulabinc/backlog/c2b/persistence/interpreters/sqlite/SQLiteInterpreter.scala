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

  override def pure[A](a: A): Task[A] = Task(a)

  override def createDatabase(): Task[Unit] = Task.deferFuture {
    db.run(createDatabaseOps)
  }

  override def getTodos: Task[Observable[CybozuDBTodo]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(todoTableOps.stream)
    )
  }

  override def getTodo(id: Id[CybozuTodo]): Task[Option[CybozuTodo]] = Task.deferFuture {
    db.run(todoTableOps.getTodo(id))
  }

  override def storeTodo(issue: CybozuDBTodo, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(todoTableOps.write(issue, writeType))
  }

  override def getTodoCount: Task[Int] = Task.deferFuture {
    db.run(todoTableOps.count)
  }

  override def storeTodoAssignees(todoId: AnyId, assigneeIds: Seq[AnyId]): Task[AnyId] = Task.deferFuture {
    db.run(cybozuIssueUserTableOps.write(todoId, assigneeIds))
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

  override def storeComment(comment: CybozuDBComment, commentType: CommentType, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    commentType match {
      case TodoComment => db.run(todoCommentTableOps.write(comment, writeType))
      case EventComment => db.run(eventCommentTableOps.write(comment, writeType))
      case ForumComment => db.run(forumCommentTableOps.write(comment, writeType))
    }
  }

  override def storeComments(comments: Seq[CybozuDBComment], commentType: CommentType, writeType: WriteType): Task[Seq[AnyId]] = Task.deferFuture {
    commentType match {
      case TodoComment => db.run(todoCommentTableOps.write(comments, writeType))
      case EventComment => db.run(eventCommentTableOps.write(comments, writeType))
      case ForumComment => db.run(forumCommentTableOps.write(comments, writeType))
    }
  }

  override def getCybozuUserById(id: AnyId): Task[Option[CybozuUser]] = Task.deferFuture {
    db.run(cybozuUserTableOps.select(Id[CybozuUser](id)))
  }

  override def getCybozuUsers(): Task[Observable[CybozuUser]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(cybozuUserTableOps.stream)
    )
  }

  override def getCybozuUserBykey(key: String): Task[Option[CybozuUser]] = Task.deferFuture {
    db.run(cybozuUserTableOps.findByKey(key))
  }

  override def storeCybozuUser(user: CybozuUser, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(cybozuUserTableOps.write(user, writeType))
  }

  override def getBacklogUsers(): Task[Observable[BacklogUser]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(backlogUserTableOps.stream)
    )
  }

  override def storeBacklogUser(user: BacklogUser, writeType: WriteType): Task[AnyId] = Task.deferFuture {
    db.run(backlogUserTableOps.write(user, writeType))
  }

  override def getBacklogPriorities(): Task[Observable[BacklogPriority]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(backlogPriorityTableOps.stream)
    )
  }

  override def storeBacklogPriorities(priorities: Seq[BacklogPriority], writeType: WriteType): Task[Seq[AnyId]] = Task.deferFuture {
    db.run(backlogPriorityTableOps.write(priorities, writeType))
  }

  override def getBacklogStatuses(): Task[Observable[BacklogStatus]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(backlogStatusTableOps.stream)
    )
  }

  override def storeBacklogStatuses(statuses: Seq[BacklogStatus], writeType: WriteType): Task[Seq[AnyId]] = Task.deferFuture {
    db.run(backlogStatusTableOps.write(statuses, writeType))
  }

  override def getCybozuPriorities(): Task[Observable[CybozuPriority]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(todoTableOps.distinctPriorities)
    )
  }

  override def getCybozuStatuses(): Task[Observable[CybozuStatus]] = Task.eval {
    Observable.fromReactivePublisher(
      db.stream(todoTableOps.distinctStatuses)
    )
  }

  override def writeDBStream[A](stream: Observable[StoreProgram[A]]): Task[Unit] = Task.deferFuture {
    stream.mapTask { prg =>
      prg.foldMap(this)
    }.foreach(_ => ())
  }
}
