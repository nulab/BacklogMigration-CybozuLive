package com.nulabinc.backlog.c2b.converters.interpreters

import cats.~>
import com.nulabinc.backlog.c2b.converters.{IssueConverter, MappingContext, UserConverter}
import com.nulabinc.backlog.c2b.converters.dsl._
import com.nulabinc.backlog.c2b.datas.Id
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import com.nulabinc.backlog.c2b.persistence.interpreters.DBInterpreter
import monix.eval.Task

case class ConvertInterpreter(dbInterpreter: DBInterpreter)(implicit ctx: MappingContext)
    extends (ConvertADT ~> Task) {


  override def apply[A](fa: ConvertADT[A]): Task[A] = fa match {
    case ConvertToBacklogUser(user) => UserConverter.toBacklogUser(user) match {
      case Right(backlogUser) => Task(backlogUser)
      case Left(error) => throw new RuntimeException("User conversion failed. " + error)
    }
    case ConvertToBacklogIssue(issue) =>
      val storePrg = for {
        maybeCreator <- StoreDSL.getUser(Id.userId(issue.creatorId))
        maybeUpdater <- StoreDSL.getUser(Id.userId(issue.updaterId))
        maybeAssignee <- StoreDSL.getUserByMaybeId(issue.assigneeId.map(Id.userId))
      } yield (maybeCreator, maybeUpdater, maybeAssignee)

      dbInterpreter.run(storePrg).map {
        case (Some(creator), Some(updater), maybeAssignee) =>
          IssueConverter.toBacklogIssue(issue, creator, updater, maybeAssignee) match {
            case Right(backlogIssue) => backlogIssue
            case Left(error) => throw new RuntimeException("Issue conversion failed. " + error)
          }
        case (_, _, _) => throw new RuntimeException("Issue conversion failed. User not found")
      }
  }

}
