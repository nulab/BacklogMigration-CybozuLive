package com.nulabinc.backlog.c2b.converters

sealed trait ConvertError[A]

case class CannotConvert[A](sourceItem: A) extends ConvertError[A]
case class MappingFiled[A](source: String) extends ConvertError[A]
