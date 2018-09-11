package com.nulabinc.backlog.c2b.dsl

import cats.free.Free

object ConsoleDSL {

  type ConsoleProgram[A] = Free[ConsoleADT, A]

  def print(str: String): ConsoleProgram[Unit] =
    Free.liftF(Print(str))

  def printBold(str: String): ConsoleProgram[Unit] =
    Free.liftF(PrintBold(str))

  def read(printMessage: String): ConsoleProgram[String] =
    Free.liftF(Read(printMessage))

}
