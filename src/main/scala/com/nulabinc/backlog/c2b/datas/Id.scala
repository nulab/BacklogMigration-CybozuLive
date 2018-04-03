package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.datas.Types.AnyId

case class Id[T] private(value: AnyId) extends AnyVal

object Id {
  def userId(id: AnyId): Id[CybozuDBUser] = Id[CybozuDBUser](id)
}

trait Entity {
  def id: AnyId
}
