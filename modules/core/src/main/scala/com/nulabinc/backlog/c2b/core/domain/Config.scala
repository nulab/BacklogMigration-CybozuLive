package com.nulabinc.backlog.c2b.core.domain

import com.nulabinc.backlog.c2b.core.domain.CommandType.Undefined

case class Config(
  backlogUrl: String = "",
  backlogKey: String = "",
  projectKey: String = "",
  commandType: CommandType = Undefined
)

sealed trait CommandType

object CommandType {
  case object Init extends CommandType
  case object Import extends CommandType
  case object Undefined extends CommandType
}


