package com.nulabinc.backlog.c2b.parser.interpreters

//import cats.Monad
//import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
//import com.nulabinc.backlog.c2b.parser.dsl.ParseADT.{Content, Result}
//import com.nulabinc.backlog.c2b.parser.dsl.{CsvParseError, ParseInterpreter}
//import zamblauskas.csv.parser._
//
//class CsvParserInterpreter extends ParseInterpreter {
//
//  import com.nulabinc.backlog.c2b.parser.formatters.ScalaCsvParserFormats._
//
//  implicit val monad = implicitly[Monad[Result]]
//
//  override def pure[A](a: Seq[A]): Result[A] = Right(a)
//
//  override def parseUser(content: Content): Result[CybozuUser] =
//    Parser.parse[CybozuUser](content) match {
//      case Right(data) => Right(data)
//      case Left(error) => Left(CsvParseError(error.lineNum, error.line, error.message))
//    }
//
//}

