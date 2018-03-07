package com.nulabinc.backlog.c2b.core

import java.util.Locale

import com.osinka.i18n.Lang
import org.slf4j.LoggerFactory

trait Logger {

  implicit val userLang =
    if (Locale.getDefault.equals(Locale.JAPAN)) Lang("ja") else Lang("en")

  val log: org.slf4j.Logger = LoggerFactory.getLogger(getClass)
}
