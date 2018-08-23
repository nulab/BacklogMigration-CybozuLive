package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.{CybozuChat, CybozuDBChat, CybozuDBComment}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIORead
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{ChatCommentTable, ChatTable, CybozuUserTable}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

private[sqlite] case class ChatTableOps()(implicit exec: ExecutionContext) extends BaseTableOps[CybozuDBChat, ChatTable] {

  protected val tableQuery = TableQuery[ChatTable]
  private val commentTableQuery = TableQuery[ChatCommentTable]
  private val cybozuUserTableQuery = TableQuery[CybozuUserTable]

  def getChat(id: AnyId): DBIORead[Option[CybozuChat]] =
    for {
      optChat <- tableQuery
        .filter(_.id === id)
        .result
        .headOption
      comments <- commentTableQuery
        .filter(_.parentId === id)
        .join(cybozuUserTableQuery)
        .on(_.creator === _.id)
        .sortBy(_._1.id.desc)
        .result
    } yield {
      optChat.map(chat =>
        CybozuChat(
          id = chat.id,
          title = chat.title,
          description = chat.description,
          comments = comments.map {
            case (comment, commentCreator) =>
              CybozuDBComment.to(comment, commentCreator)
          }
        )
      )
    }
}
