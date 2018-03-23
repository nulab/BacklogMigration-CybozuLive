package com.nulabinc.backlog.c2b.interpreters

import backlog4s.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram

object syntax {

  implicit class ResponseOps[A](response: Response[A]) {

    def orExit(successMessage: String, failureMessage: String): AppProgram[Unit] =
      response match {
        case Right(_) => AppDSL.fromConsole(ConsoleDSL.print(successMessage))
        case Left(error) => AppDSL.exit(error.toString, 1)
      }
  }
}
