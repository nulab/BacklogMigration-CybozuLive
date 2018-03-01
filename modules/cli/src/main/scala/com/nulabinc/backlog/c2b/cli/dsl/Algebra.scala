package com.nulabinc.backlog.c2b.cli.dsl

import com.nulabinc.backlog.c2b.core.domain.CliParam

private[cli] object Algebra {

  sealed trait CliParser[+A]

  case class Parse(args: Seq[String]) extends CliParser[CliParam]
}
