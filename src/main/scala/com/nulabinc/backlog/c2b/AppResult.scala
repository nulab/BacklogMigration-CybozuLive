package com.nulabinc.backlog.c2b

trait AppResult

case object Success extends AppResult
case object ConfigError extends AppResult
case class Error(ex: Throwable) extends AppResult