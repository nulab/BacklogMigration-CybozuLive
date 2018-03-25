package com.nulabinc.backlog.c2b

import java.io.PrintStream

import com.nulabinc.backlog.c2b.core.Logger
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi.ansi

private[c2b] object Console extends Logger {

  val outStream: PrintStream = System.out

  def error(value: String, space: Int = 0): PrintStream = {
    println(value, space, RED)
  }

  def success(value: String, space: Int = 0): PrintStream = {
    println(value, space, GREEN)
  }

  def warning(value: String, space: Int = 0): PrintStream = {
    println(value, space, YELLOW)
  }

  def info(value: String, space: Int = 0): PrintStream = {
    println(value, space, BLUE)
  }

  def println(value: String, space: Int = 0, color: Ansi.Color = BLACK): PrintStream = {
    log.info(value)
    if (color == BLACK) {
      outStream.println((" " * space) + ansi().a(value).reset().toString)
    } else {
      outStream.println((" " * space) + ansi().fg(color).a(value).reset().toString)
    }

    outStream.flush()
    outStream
  }

  def bold(value: String, color: Ansi.Color = BLACK): String = {
    if (color == BLACK) {
      ansi().bold().a(value).reset().toString
    } else {
      ansi().fg(color).bold().a(value).reset().toString
    }
  }

  def boldln(value: String, space: Int = 0, color: Ansi.Color = BLACK): PrintStream = {
    log.info(value)
    outStream.println((" " * space) + bold(value, color))
    outStream.flush()
    outStream
  }

  def overwrite(value: String, space: Int = 0): Unit = {
    log.info(value)

    synchronized {
      outStream.print(ansi.cursorLeft(999).cursorUp(1).eraseLine(Ansi.Erase.ALL))
      outStream.flush()
      outStream.println((" " * space) + value)
    }
  }

  def printBanner(applicationName: String, applicationVersion: String): Unit = println(
    s"""
      |$applicationName $applicationVersion
      |--------------------------------------------------""".stripMargin)

  def printError(ex: Throwable): Unit =
    error(ex.getMessage)

}
