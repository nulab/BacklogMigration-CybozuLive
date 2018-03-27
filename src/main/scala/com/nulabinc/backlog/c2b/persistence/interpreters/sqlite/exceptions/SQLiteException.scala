package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.exceptions

import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.exceptions.SQLiteError.SQLiteError

object SQLiteError extends Enumeration {
  type SQLiteError = Value

  val IdNotGenerated = Value("Id is not generated")
}

case class SQLiteException(error: SQLiteError) extends RuntimeException {
  override def getMessage: String =
    s"Error from sqlite ${error.toString}"
}
