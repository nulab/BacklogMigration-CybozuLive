package com.nulabinc.backlog.c2b.exceptions

case class CybozuLiveImporterException(message: String) extends RuntimeException {
  override def getMessage: String = message
}