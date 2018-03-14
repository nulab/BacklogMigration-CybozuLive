//package com.nulabinc.backlog.c2b.converters.dsl
//
//import cats.free.Free
//import com.nulabinc.backlog.c2b.datas._
//import com.nulabinc.backlog.migration.common.domain._
//
//object ConvertDSL {
//
//  type ConvertProgram[A] = Free[ConvertADT, A]
//
//  def convertToBacklogUser(cybozuUser: CybozuUser): ConvertProgram[BacklogUser] =
//    Free.liftF(ConvertToBacklogUser(cybozuUser))
//
//  def convertToBacklogIssue(cybozuIssue: CybozuIssue): ConvertProgram[BacklogIssue] =
//    Free.liftF(ConvertToBacklogIssue(cybozuIssue))
//
//}
