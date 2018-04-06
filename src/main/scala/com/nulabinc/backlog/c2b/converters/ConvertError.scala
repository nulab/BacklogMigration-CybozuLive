package com.nulabinc.backlog.c2b.converters

sealed trait ConvertError

case class CannotConvert[A](sourceItem: A) extends ConvertError
case class MappingFail[A](source: String) extends ConvertError
