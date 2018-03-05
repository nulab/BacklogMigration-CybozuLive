package com.nulabinc.backlog.c2b.parser.dsl

import com.nulabinc.backlog.c2b.parser.dsl.ParseADT.Result
import com.nulabinc.backlog.c2b.parser.dsl.ParseDsl.ParsePrg

object syntax {

//  import ParseOp._
//
//  implicit class ResultOps[A](result: Result[A]) {
//    def orFail: ParsePrg[A] =
//      result match {
//        case Right(value) => pure(value.asInstanceOf[Seq[A]])
//        case Left(error) => throw new RuntimeException(error.toString) // TODO
//      }
//  }
//
//  implicit class ApiOps[A](apiPrg: ParsePrg[Result[A]]) {
//    def orFail: ParsePrg[A] =
//      apiPrg.flatMap {
//        case Right(value) => pure(value)
//        case Left(error) => throw new RuntimeException(error.toString) // TODO
//      }
//  }
}