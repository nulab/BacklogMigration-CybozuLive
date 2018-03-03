package com.nulabinc.backlog.c2b.utils

object ClassVersionChecker {

  private val CLASS_VERSION_8: Double = 52.0

  def check(): Boolean =
    System.getProperty("java.class.version").toDouble >= CLASS_VERSION_8

}

