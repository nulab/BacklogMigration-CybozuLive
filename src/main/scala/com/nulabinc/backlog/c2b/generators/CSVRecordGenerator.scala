package com.nulabinc.backlog.c2b.generators

import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object CSVRecordGenerator {

  val charset: Charset = Charset.forName("UTF-8")

  def backlogUserToByteArray(user: Observable[BacklogUser]): Observable[Array[Byte]] =
    user.map { item =>
      s""""${item.userId.getOrElse("")}",""\n""".stripMargin.getBytes(charset)
    }

  def cybozuUserToByteArray(user: Observable[CybozuUser]): Observable[Array[Byte]] =
    user.map { item =>
      s""""","${item.userId}"\n""".stripMargin.getBytes(charset)
    }

  def backlogPriorityToByteArray(priority: Observable[BacklogPriority]): Observable[Array[Byte]] =
    priority.map { u =>
      s""""","${u.name}"\n""".stripMargin.getBytes(charset)
    }

  def cybozuPriorityToByteArray(priority: Observable[CybozuPriority]): Observable[Array[Byte]] =
    priority.map { u =>
      s""""${u.value}",""\n""".stripMargin.getBytes(charset)
    }

  def backlogStatusToByteArray(status: Observable[BacklogStatus]): Observable[Array[Byte]] =
    status.map { u =>
      s""""${u.name}",""\n""".stripMargin.getBytes(charset)
    }

  def cybozuStatusToByteArray(status: Observable[CybozuStatus]): Observable[Array[Byte]] =
    status.map { u =>
      s""""${u.value}",""\n""".stripMargin.getBytes(charset)
    }

  def headerToByteArray: Observable[Array[Byte]] =
    Observable {
      """"CybozuLive","Backlog"\n""".stripMargin.getBytes(charset)
    }

  def splitToByteArray(): Observable[Array[Byte]] =
    Observable {
      s""""----------------------","----------------------"\n""".stripMargin.getBytes(charset)
    }

}
