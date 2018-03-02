package com.nulabinc.backlog.c2b.core.domain

import com.nulabinc.backlog.c2b.core.domain.Config._


case class Config(
  backlogUrl: String = "",
  backlogKey: String = "",
  projectKey: String = "",
  commandType: CommandType = Undefined
)

object Config {

  sealed trait CommandType

  case object Init extends CommandType
  case object Import extends CommandType
  case object Undefined extends CommandType
}



