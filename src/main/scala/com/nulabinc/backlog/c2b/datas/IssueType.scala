package com.nulabinc.backlog.c2b.datas

sealed trait IssueType

object IssueType {
  case object ToDo extends IssueType
  case object Event extends IssueType
  case object Forum extends IssueType
  case object Chat extends IssueType
}
