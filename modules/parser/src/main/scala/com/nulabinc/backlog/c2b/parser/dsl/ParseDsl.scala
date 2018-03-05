package com.nulabinc.backlog.c2b.parser.dsl

import cats.free.Free

object ParseDsl {

  type ParsePrg[A] = Free[ParseADT, A]

}
