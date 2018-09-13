package com.nulabinc.backlog.c2b.dsl

import cats.free.Free

object HttpDSL {

  import HttpADT._

  type HttpProgram[A] = Free[HttpADT, A]

  def get(uri: String): HttpProgram[Response[String]] =
    Free.liftF(Get(uri))

}
