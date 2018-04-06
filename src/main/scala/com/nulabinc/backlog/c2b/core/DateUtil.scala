package com.nulabinc.backlog.c2b.core

import java.util.Date

import com.nulabinc.backlog.c2b.datas.Types.DateTime
import com.nulabinc.backlog.migration.common.utils.{DateUtil => MigrationDateUtil}

object DateUtil {

  def toDate(dateTime: DateTime): Date =
    Date.from(dateTime.toInstant)

  def toDateString(dateTime: DateTime): String =
    MigrationDateUtil.dateFormat(toDate(dateTime))

  def toDateTimeString(dateTime: DateTime): String =
    MigrationDateUtil.isoFormat(toDate(dateTime))
}
