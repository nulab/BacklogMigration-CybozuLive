package com.nulabinc.backlog.c2b.dsl

import com.nulabinc.backlog.c2b.dsl.HttpADT.Response

sealed trait HttpADT[A]
case class Get(uri: String) extends HttpADT[Response[String]]

sealed trait HttpError
case class RequestError(error: String) extends HttpError
case class InvalidResponse(msg: String) extends HttpError
case object ServerDown extends HttpError

object HttpADT {
  type Response[A] = Either[HttpError, A]
}
