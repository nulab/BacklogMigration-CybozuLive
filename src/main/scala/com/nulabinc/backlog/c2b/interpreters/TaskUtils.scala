package com.nulabinc.backlog.c2b.interpreters

import monix.eval.Task
import monix.execution.Scheduler

object TaskUtils {
  case class Suspend[A](eval: () => Task[A])

  def sequential[A](prgs: Seq[Suspend[A]])(implicit exc: Scheduler): Task[Seq[A]] = {
    prgs.foldLeft(Task(Seq.empty[A])) {
      case (acc, future) =>
        acc.flatMap(res => future.eval().map(res2 => res :+ res2))
    }
  }

}
