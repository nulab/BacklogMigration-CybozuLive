package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.AnyId

case class Id[T] private(value: AnyId) extends AnyVal

object Id {
  def userId(id: AnyId): Id[CybozuUser] = Id[CybozuUser](id)
  def todoId(id: AnyId): Id[CybozuTodo] = Id[CybozuTodo](id)
}

trait Entity {
  def id: AnyId
}
