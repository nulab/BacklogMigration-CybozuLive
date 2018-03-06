package com.nulabinc.backlog.c2b.parser

import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
import com.nulabinc.backlog.c2b.core.utils.Logger

object CsvParser extends Logger {

  // "姓","名","よみがな姓","よみがな名","メールアドレス"
  def user(line: String): CybozuUser = {
    val fields = line.split(",").map(_.replace("\"", ""))
    try {
      CybozuUser(
        lastName      = fields(0),
        firstName     = fields(1),
        emailAddress  = fields(4)
      )
    } catch {
      case t: Throwable =>
        log.error(s"Unable to parse fields $fields: ${t.getMessage}")
        throw t
    }
  }

}
