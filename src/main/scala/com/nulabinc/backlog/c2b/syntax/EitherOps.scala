package com.nulabinc.backlog.c2b.syntax

object EitherOps {
  implicit class SeqEitherOps[E, B](results: Seq[Either[E, B]]) {
    def sequence: Either[E, Seq[B]] =
      results.foldLeft(Right(Seq.empty[B]): Either[E, Seq[B]]) {
        case (acc, Left(_)) => acc
        case (acc, Right(item)) => acc.map(_ :+ item)
      }
  }
}
