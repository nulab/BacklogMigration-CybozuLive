package com.nulabinc.backlog.c2b.syntax

import com.github.chaabaj.backlog4s.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}

object BacklogResponseOps {
  implicit class ResponseOps[A](response: Response[A]) {
    def orExit(successMessage: String, failureMessage: String): AppProgram[Unit] =
      response match {
        case Right(_) => AppDSL.fromConsole(ConsoleDSL.print(successMessage))
        case Left(_) => AppDSL.exit(failureMessage, 1)
      }
  }
}
