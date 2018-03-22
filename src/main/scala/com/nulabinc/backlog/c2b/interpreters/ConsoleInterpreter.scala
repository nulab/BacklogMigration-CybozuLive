package com.nulabinc.backlog.c2b.interpreters

import cats.free.Free
import cats.~>
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL.ConsoleProgram
import monix.eval.Task

sealed trait ConsoleADT[A]
case class Print(str: String) extends ConsoleADT[Unit]
case class Read(printMessage: String) extends ConsoleADT[String]

object ConsoleDSL {

  type ConsoleProgram[A] = Free[ConsoleADT, A]

  def print(str: String): ConsoleProgram[Unit] =
    Free.liftF(Print(str))

  def read(printMessage: String): ConsoleProgram[String] =
    Free.liftF(Read(printMessage))

}

class ConsoleInterpreter extends (ConsoleADT ~> Task) {

  def run[A](program: ConsoleProgram[A]): Task[A] =
    program.foldMap(this)

  def apply[A](fa: ConsoleADT[A]): Task[A] = fa match  {
    case Print(str) => Task {
      Console.println(str)
      ()
    }
    case Read(printMessage) => Task {
      scala.io.StdIn.readLine(printMessage)
    }
  }
}
