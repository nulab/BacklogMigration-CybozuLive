package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.persistence.dsl.StoreADT
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import monix.eval.Task

trait StoreInterpreter extends (StoreADT ~> Task) {

  def run[A](prg: StoreProgram[A]): Task[A]
}
