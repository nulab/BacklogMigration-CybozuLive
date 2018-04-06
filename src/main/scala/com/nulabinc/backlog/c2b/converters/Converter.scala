package com.nulabinc.backlog.c2b.converters

trait Converter[A, B] {
  def to(a: A): Either[ConvertError, B]
  def to(a: Option[A]): Either[ConvertError, Option[B]] =
    a.map(to).map { result =>
      result.map(Some(_): Option[B])
    }.getOrElse(Right(None))
}