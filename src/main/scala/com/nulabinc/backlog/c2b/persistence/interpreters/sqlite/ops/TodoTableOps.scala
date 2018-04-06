package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.{DBIORead, DBIOStream}
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables._
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

private[sqlite] case class TodoTableOps()(implicit exc: ExecutionContext) extends BaseTableOps[CybozuDBTodo, TodoTable] {

  import JdbcMapper._

  protected val tableQuery = TableQuery[TodoTable]
  private val commentTableQuery = TableQuery[CommentTable]
  private val cybozuUserTableQuery = TableQuery[CybozuUserTable]
  private val issueUserTableQuery = TableQuery[CybozuIssueUserTable]

  lazy val distinctPriorities: DBIOStream[CybozuDBPriority] =
    tableQuery
      .map(_.priority)
      .distinct
      .result

  lazy val distinctStatuses: DBIOStream[CybozuDBStatus] =
    tableQuery
      .map(_.status)
      .distinct
      .result

  def getTodo(id: AnyId): DBIORead[Option[CybozuTodo]] = {
    for {
      optTodo <- tableQuery
        .filter(_.id === id)
        .join(cybozuUserTableQuery)
        .on(_.creator === _.id)
        .join(cybozuUserTableQuery)
        .on(_._1.updater === _.id)
        .result
        .headOption
      comments <- commentTableQuery
        .filter(_.parentId === id)
        .join(cybozuUserTableQuery)
        .on(_.parentId === _.id)
        .result
      // SELECT userfields... FROM issue_user JOIN cybozu_user ON cybozu_user.userId = id WHERE issueId = ?
      assignees <- issueUserTableQuery
          .filter(_.issueId === id)
          .join(cybozuUserTableQuery)
          .on(_.userId === _.id)
          .map(_._2)
          .result
    } yield {
      optTodo.map {
        case ((todo, creator), updater) =>
          CybozuTodo(
            todo = todo,
            comments = comments.map {
              case (comment, commentCreator) =>
                CybozuComment(comment, commentCreator)
            },
            creator = creator,
            updater = updater,
            assignees = assignees
          )
      }
    }
  }
}