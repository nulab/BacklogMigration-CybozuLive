package com.nulabinc.backlog.c2b.syntax

import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram

import scala.util.{Failure, Success, Try}

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

  implicit class TryAppProgramOps[A](program: AppProgram[Try[A]]) {
    def orFail: AppProgram[A] =
      program.map {
        case Success(value) => value
        case Failure(ex) => throw ex
      }
  }
}
