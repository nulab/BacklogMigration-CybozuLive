package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables

import com.nulabinc.backlog.c2b.datas.{CybozuDBComment, CybozuUser}
import com.nulabinc.backlog.c2b.datas.Types.{AnyId, DateTime}
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.SQLiteProfile.api._

private[sqlite] abstract class CommentTable(tag: Tag, name: String) extends BaseTable[CybozuDBComment](tag, name) {

  import JdbcMapper._

  def parentId: Rep[AnyId] = column[AnyId]("parent_id")
  def creator: Rep[AnyId] = column[AnyId]("creator_id")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")
  def content: Rep[String] = column[String]("content")

  override def * : ProvenShape[CybozuDBComment] =
    (id, parentId, creator, createdAt, content) <> (CybozuDBComment.tupled, CybozuDBComment.unapply)
}

private[sqlite] class ToDoCommentTable(tag: Tag) extends CommentTable(tag, "cybozu_todo_comments")
private[sqlite] class EventCommentTable(tag: Tag) extends CommentTable(tag, "cybozu_event_comments")
private[sqlite] class ForumCommentTable(tag: Tag) extends CommentTable(tag, "cybozu_forum_comments")
private[sqlite] class ChatCommentTable(tag: Tag) extends CommentTable(tag, "cybozu_chat_comments")