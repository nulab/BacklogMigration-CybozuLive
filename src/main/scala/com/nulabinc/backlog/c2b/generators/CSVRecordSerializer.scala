package com.nulabinc.backlog.c2b.generators

import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas._

trait Serializer[A, B] {
  def serialize(a: A): B
}

object CSVRecordSerializer {

  val charset: Charset = Charset.forName("UTF-8")

  implicit val backlogUserSerializer: Serializer[BacklogUser, Array[Byte]] =
    (user: BacklogUser) =>
      s""""","${user.userId.getOrElse("")}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuUserSerializer: Serializer[CybozuUser, Array[Byte]] =
    (user: CybozuUser) =>
      s""""${user.userId}",""\n""".stripMargin.getBytes(charset)

  implicit val backlogPrioritySerializer: Serializer[BacklogPriority, Array[Byte]] =
    (priority: BacklogPriority) =>
      s""""","${priority.name}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuPrioritySerializer: Serializer[CybozuPriority, Array[Byte]] =
    (priority: CybozuPriority) =>
      s""""${priority.value}",""\n""".stripMargin.getBytes(charset)

  implicit val backlogStatusSerializer: Serializer[BacklogStatus, Array[Byte]] =
    (status: BacklogStatus) =>
      s""""","${status.name}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuStatusSerializer: Serializer[CybozuStatus, Array[Byte]] =
    (status: CybozuStatus) =>
      s""""${status.value}",""\n""".stripMargin.getBytes(charset)

  def serialize[A](a: A)(implicit serializer: Serializer[A, Array[Byte]]): Array[Byte] =
    serializer.serialize(a)

  def header: Array[Byte] =
    s""""CybozuLive","Backlog"\n""".stripMargin.getBytes(charset)

  def split: Array[Byte] =
    s""""----------------------","----------------------"\n""".stripMargin.getBytes(charset)
}
