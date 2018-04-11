package com.nulabinc.backlog.c2b.core

import java.nio.file.Paths

import com.osinka.i18n.Messages

import scala.util.{Success, Try}

object DataDirectoryChecker extends Logger {

  def check(pathString: String) = Try {
    try {
      Paths.get(pathString).toRealPath()
      Success
    } catch {
      case _: Throwable =>
        val message = Messages("error.data_folder_not_found", pathString)
        throw new RuntimeException(message)
    }
  }
}
