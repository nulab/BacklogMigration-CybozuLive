package com.nulabinc.backlog.c2b.syntax

import com.github.chaabaj.backlog4s.dsl.HttpADT.Response
import com.nulabinc.backlog.c2b.dsl.AppDSL
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.exceptions.CybozuLiveImporterException
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL

object BacklogResponseOps {
  implicit class ResponseOps[A](response: Response[A]) {
    def orExit(successMessage: String, failureMessage: String): AppProgram[Unit] =
      response match {
        case Right(_) => AppDSL.fromConsole(ConsoleDSL.print(successMessage))
        case Left(_) => throw CybozuLiveImporterException(failureMessage)
      }
  }
}
