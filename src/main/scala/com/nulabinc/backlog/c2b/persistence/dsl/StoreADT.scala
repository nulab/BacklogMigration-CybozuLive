package com.nulabinc.backlog.c2b.persistence.dsl

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.reactive.Observable

sealed trait StoreADT[A]

sealed trait WriteType
case object Insert extends WriteType
case object Update extends WriteType

sealed trait CommentType
case object TodoComment extends CommentType
case object EventComment extends CommentType
case object ForumComment extends CommentType
case object ChatComment extends CommentType

case class Pure[A](a: A) extends StoreADT[A]

case object CreateDatabase extends StoreADT[Unit]

case object GetTodos extends StoreADT[Observable[CybozuDBTodo]]
case object GetTodoCount extends StoreADT[Int]
case class GetTodo(id: Id[CybozuTodo]) extends StoreADT[Option[CybozuTodo]]
case class StoreTodo(todo: CybozuDBTodo, writeType: WriteType = Insert) extends StoreADT[AnyId]

case class StoreComment(comment: CybozuDBComment, commentType: CommentType, writeType: WriteType = Insert) extends StoreADT[AnyId]
case class StoreComments(comments: Seq[CybozuDBComment], commentType: CommentType, writeType: WriteType = Insert) extends StoreADT[Seq[AnyId]]

case class StoreTodoAssignees(todoId: AnyId, assigneeIds: Seq[AnyId]) extends StoreADT[Int]

case object GetEvents extends StoreADT[Observable[CybozuDBEvent]]
case object GetEventCount extends StoreADT[Int]
case class GetEvent(id: AnyId) extends StoreADT[Option[CybozuEvent]]
case class StoreEvent(event: CybozuDBEvent, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetForums extends StoreADT[Observable[CybozuDBForum]]
case object GetForumCount extends StoreADT[Int]
case class GetForum(id: AnyId) extends StoreADT[Option[CybozuForum]]
case class StoreForum(forum: CybozuDBForum, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetChats extends StoreADT[Observable[CybozuDBChat]]
case object GetChatCount extends StoreADT[Int]
case class GetChat(id: AnyId) extends StoreADT[Option[CybozuChat]]
case class StoreChat(chat: CybozuDBChat, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetCybozuUsers extends StoreADT[Observable[CybozuUser]]
case class GetCybozuUserById(id: AnyId) extends StoreADT[Option[CybozuUser]]
case class GetCybozuUserBykey(key: String) extends StoreADT[Option[CybozuUser]]
case class StoreCybozuUser(user: CybozuUser, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetCybozuPriorities extends StoreADT[Observable[CybozuPriority]]
case object GetCybozuStatuses extends StoreADT[Observable[CybozuStatus]]

case class WriteDBStream[A](stream: Observable[StoreProgram[A]]) extends StoreADT[Unit]

case object GetBacklogUsers extends StoreADT[Observable[BacklogUser]]
case class StoreBacklogUser(user: BacklogUser, writeType: WriteType = Insert) extends StoreADT[AnyId]

case object GetBacklogPriorities extends StoreADT[Observable[BacklogPriority]]
case class StoreBacklogPriorities(priorities: Seq[BacklogPriority], writeType: WriteType = Insert)
  extends StoreADT[Seq[AnyId]]

case object GetBacklogStatuses extends StoreADT[Observable[BacklogStatus]]
case class StoreBacklogStatuses(statuses: Seq[BacklogStatus], writeType: WriteType = Insert)
  extends StoreADT[Seq[AnyId]]
