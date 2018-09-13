package com.nulabinc.backlog.c2b.dsl

sealed trait ConsoleADT[A]
case class Print(str: String) extends ConsoleADT[Unit]
case class PrintBold(str: String) extends ConsoleADT[Unit]
case class PrintWarning(str: String) extends ConsoleADT[Unit]
case class Read(printMessage: String) extends ConsoleADT[String]
