package com.nulabinc.backlog.c2b.persistence.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.persistence.dsl.StorageADT
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import monix.eval.Task

trait StorageInterpreter extends (StorageADT ~> Task) {

  def run[A](prg: StorageProgram[A]): Task[A]
}
