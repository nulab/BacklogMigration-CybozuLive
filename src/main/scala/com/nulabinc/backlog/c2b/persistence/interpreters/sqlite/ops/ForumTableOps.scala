package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.{CybozuComment, CybozuDBForum, CybozuForum}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIORead
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{CommentTable, CybozuUserTable, ForumTable}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

private[sqlite] case class ForumTableOps()(implicit exc: ExecutionContext) extends BaseTableOps[CybozuDBForum, ForumTable] {

  protected val tableQuery = TableQuery[ForumTable]
  private val commentTableQuery = TableQuery[CommentTable]
  private val cybozuUserTableQuery = TableQuery[CybozuUserTable]

  lazy val count: DBIORead[Int] =
    tableQuery
      .length
      .result

  def getForum(id: AnyId): DBIORead[Option[CybozuForum]] = {
    for {
      optForum <- tableQuery
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
    } yield {
      optForum.map {
        case ((forum, creator), updater) =>
          CybozuForum(
            id = forum.id,
            title = forum.title,
            content = forum.content,
            creator = creator,
            createdAt = forum.createdAt,
            updater = updater,
            updatedAt = forum.updatedAt,
            comments = comments.map {
              case (comment, commentCreator) =>
                CybozuComment(
                  id = comment.id,
                  parentId = comment.parentId,
                  creator = commentCreator,
                  createdAt = comment.createdAt,
                  content = comment.content
                )
            }
          )
      }
    }
  }
}
