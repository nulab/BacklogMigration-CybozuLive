package com.nulabinc.backlog.c2b.serializers

import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.datas._

trait Serializer[A, B] {
  def serialize(a: A): B
}

trait ToTuple[A, B, C, D] {
  def toTuple(a: A, b: B): (C, D)
}

object CSVRecordSerializer {

  private val charset = Config.mappingFileCharset

  implicit val backlogUserSerializer: Serializer[BacklogUser, Array[Byte]] =
    (user: BacklogUser) =>
      s""""${user.name}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuUserSerializer: Serializer[CybozuUser, Array[Byte]] =
    (user: CybozuUser) =>
      s""""${user.userId}",""\n""".stripMargin.getBytes(charset)

  implicit val backlogPrioritySerializer: Serializer[BacklogPriority, Array[Byte]] =
    (priority: BacklogPriority) =>
      s""""${priority.name}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuPrioritySerializer: Serializer[CybozuPriority, Array[Byte]] =
    (priority: CybozuPriority) =>
      s""""${priority.value}",""\n""".stripMargin.getBytes(charset)

  implicit val backlogStatusSerializer: Serializer[BacklogStatus, Array[Byte]] =
    (status: BacklogStatus) =>
      s""""${status.name}"\n""".stripMargin.getBytes(charset)

  implicit val cybozuStatusSerializer: Serializer[CybozuStatus, Array[Byte]] =
    (status: CybozuStatus) =>
      s""""${status.value}",""\n""".stripMargin.getBytes(charset)

  implicit val stringTupleSerializer: Serializer[(String, String), Array[Byte]] =
    (tuple) =>
      s""""${tuple._1}","${tuple._2}"\n""".getBytes(charset)

  def serialize[A](a: A)(implicit serializer: Serializer[A, Array[Byte]]): Array[Byte] =
    serializer.serialize(a)

  def serializeMap(mapping: Map[String, String]): IndexedSeq[Array[Byte]] =
    mapping.foldLeft(IndexedSeq.empty[Array[Byte]]) {
      case (acc, entry) =>
        acc :+ CSVRecordSerializer.serialize(entry)
    }

  def header: Array[Byte] =
    s""""CybozuLive","Backlog"\n""".stripMargin.getBytes(charset)

  def backlogHeader(mappingName: String): Array[Byte] =
    s""""$mappingName"\n""".stripMargin.getBytes(charset)

}
