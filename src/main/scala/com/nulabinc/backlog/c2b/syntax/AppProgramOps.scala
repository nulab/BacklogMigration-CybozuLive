package com.nulabinc.backlog.c2b.syntax

import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram

object AppProgramOps {
  implicit class SeqAppProgramOps[A](programs: Seq[AppProgram[A]]) {
    def sequence: AppProgram[Seq[A]] =
      programs.foldLeft(AppDSL.pure(Seq.empty[A])) {
        case (newPrg, prg) =>
          newPrg.flatMap { results =>
            prg.map { result =>
              results :+ result
            }
          }
      }
  }
}
