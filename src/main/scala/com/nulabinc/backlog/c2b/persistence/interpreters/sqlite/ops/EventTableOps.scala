package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.{CybozuDBComment, CybozuDBEvent, CybozuEvent}
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIORead
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.{CommentTable, CybozuUserTable, EventTable}
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

private[sqlite] case class EventTableOps()(implicit exc: ExecutionContext) extends BaseTableOps[CybozuDBEvent, EventTable] {

  protected val tableQuery = TableQuery[EventTable]
  private val commentTableQuery = TableQuery[CommentTable]
  private val cybozuUserTableQuery = TableQuery[CybozuUserTable]

  lazy val count: DBIORead[Int] =
    tableQuery
      .length
      .result

  def getEvent(id: AnyId): DBIORead[Option[CybozuEvent]] =
    for {
      optEvent <- tableQuery
        .filter(_.id === id)
        .join(cybozuUserTableQuery)
        .on(_.creator === _.id)
        .result
        .headOption
      comments <- commentTableQuery
        .filter(_.parentId === id)
        .join(cybozuUserTableQuery)
        .on(_.creator === _.id)
        .result
    } yield {
      optEvent.map {
        case (event, creator) =>
          CybozuEvent(
            id = event.id,
            startDateTime = event.startDateTime,
            endDateTime = event.endDateTime,
            menu = event.menu,
            title = event.title,
            memo = event.memo,
            comments = comments.map {
              case (comment, commentCreator) =>
                CybozuDBComment.to(comment, commentCreator)
            },
            creator = creator
          )
      }
    }

}
