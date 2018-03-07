package com.nulabinc.backlog.c2b.utils

import com.nulabinc.backlog.c2b.core.Logger
import com.osinka.i18n.Messages

import scala.util.{Failure, Success, Try}

object ClassVersionChecker extends Logger {

  private val CLASS_VERSION_8: Double = 52.0

  def check() = Try {
    if(System.getProperty("java.class.version").toDouble >= CLASS_VERSION_8)
      Success
    else {
      val message = Messages("error.require_java8", System.getProperty("java.specification.version"))
      Failure(throw new RuntimeException(message))
    }
  }


}

