package com.nulabinc.backlog.c2b.cli.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.core.domain.CliParam

object Cli {

  import Algebra._

  type ParseProgram[A] = Free[CliParser, A]

  def parse(args: Seq[String]): ParseProgram[CliParam] =
    Free.liftF(Parse(args))

}
