package com.nulabinc.backlog.c2b.parser.dsl

import cats.free.Free
import cats.{Monad, ~>}
import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
import com.nulabinc.backlog.c2b.parser.dsl.ParseADT.{Content, Result}
import com.nulabinc.backlog.c2b.parser.dsl.ParseDsl.ParsePrg

sealed trait ParseError
case class CsvParseError(lineNum: Int, line: String, message: String) extends ParseError

object ParseADT {
  type Content = String
  type Result[A] = Either[ParseError, Seq[A]]
}

//sealed trait ParseADT[A]
//private[dsl] case class ParseUsers[A](content: String) extends ParseADT[Result[A]]
//private[dsl] case class Pure[A](a: Seq[A]) extends ParseADT[A]
//
//object ParseOp {
//
//  type ParseF[A] = Free[ParseADT, A]
//
//  def pure[A](a: Seq[A]): ParseF[A] =
//    Free.liftF(Pure(a))
//
//  def parseUsers[A](content: Content): ParseF[Result[A]] =
//    Free.liftF[ParseADT, Result[A]](ParseUsers(content))
//
//}

//trait ParseInterpreter extends (ParseADT ~> Result) {
//
//  implicit def monad: Monad[Result]
//
//  def pure[A](a: Seq[A]): Result[A]
//
//  def run[A](parsePrg: ParsePrg[A]): Result[A] = parsePrg.foldMap(this)
//
//  def parseUser(content: Content): Result[CybozuUser]
//
//  override def apply[A](fa: ParseADT[A]): Result[A] = fa match {
//    case Pure(a) => pure(a)
//    case ParseUsers(content) => parseUser(content)
//  }
////  override def apply[A](fa: ParseADT[A]): Result[A] = ???
//}