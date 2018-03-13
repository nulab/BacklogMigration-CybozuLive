package com.nulabinc.backlog.c2b.converters.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.converters.{MappingContext, UserConverter}
import com.nulabinc.backlog.c2b.converters.dsl.{ConvertADT, ConvertToBacklogIssue, ConvertToBacklogUser}
import com.nulabinc.backlog.c2b.datas.Id
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task

case class ConvertInterpreter(dbInterpreter: DBInterpreter)(implicit ctx: MappingContext)
    extends (ConvertADT ~> Task) {


  override def apply[A](fa: ConvertADT[A]): Task[A] = ???
//  {
//
//    fa match {
//      case ConvertToBacklogUser(user) => Task(UserConverter.toBacklogUser(user))
//      case ConvertToBacklogIssue(issue) =>
//        val storeProgram = for {
//          creator <- StoreDSL.getUser(Id.userId(issue.creatorId))
//        } yield creator
////        val a = dbInterpreter.run(storeProgram)
////        a.flatMap(b => b.)
//    }
//  }

}
