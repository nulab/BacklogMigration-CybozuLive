package com.nulabinc.backlog.c2b.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.dsl._
import com.nulabinc.backlog.c2b.dsl.ConsoleDSL.ConsoleProgram
import com.nulabinc.backlog.migration.common.utils.ConsoleOut
import monix.eval.Task

class ConsoleInterpreter extends (ConsoleADT ~> Task) {

  def run[A](program: ConsoleProgram[A]): Task[A] =
    program.foldMap(this)

  def print(string: String): Task[Unit] = Task {
    if (string.nonEmpty)
      ConsoleOut.println(string)
  }

  def printBold(string: String): Task[Unit] = Task {
    if (string.nonEmpty)
      ConsoleOut.boldln(string)
  }

  def printWarning(string: String): Task[Unit] = Task {
    if (string.nonEmpty)
      ConsoleOut.warning(string)
  }

  def read(printMessage: String): Task[String] = Task {
    scala.io.StdIn.readLine(printMessage)
  }

  def apply[A](fa: ConsoleADT[A]): Task[A] = fa match  {
    case Print(str) => print(str)
    case PrintBold(str) => printBold(str)
    case PrintWarning(str) => printWarning(str)
    case Read(printMessage) => read(printMessage)
  }
}
