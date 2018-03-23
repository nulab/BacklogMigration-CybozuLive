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

  def print(string: String): Task[Unit] = Task {
    Console.println(string)
    ()
  }

  def read(printMessage: String): Task[String] = Task {
    scala.io.StdIn.readLine(printMessage)
  }

  def apply[A](fa: ConsoleADT[A]): Task[A] = fa match  {
    case Print(str) => print(str)
    case Read(printMessage) => read(printMessage)
  }
}
