package com.nulabinc.backlog.c2b.services

import com.github.chaabaj.backlog4s.apis.{PriorityApi, StatusApi, UserApi}
import com.nulabinc.backlog.c2b.datas.{BacklogPriority, BacklogStatus, BacklogUser}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import monix.reactive.Observable

object BacklogService {

  import com.github.chaabaj.backlog4s.dsl.syntax._

  def storeUsers(api: UserApi): AppProgram[Unit] =
    for {
      users <- AppDSL.fromBacklog(api.all.orFail)
      _ <- AppDSL.consumeStream(
        Observable.fromIterator(users.iterator).map { user =>
          AppDSL.fromStore(StoreDSL.storeBacklogUser(BacklogUser.from(user))).map(_ => ())
        }
      )
    } yield ()

  def storePriorities(api: PriorityApi): AppProgram[Unit] =
    for {
      backlogPriorities <- AppDSL.fromBacklog(api.all)
      _ <- backlogPriorities match {
        case Right(data) =>
          val items = data.map(p => BacklogPriority(0, p.name))
          AppDSL.fromStore(StoreDSL.storeBacklogPriorities(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()

  def storeStatuses(api: StatusApi): AppProgram[Unit] =
    for {
      backlogStatuses <- AppDSL.fromBacklog(api.all)
      _ <- backlogStatuses match {
        case Right(data) =>
          val items = data.map(p => BacklogStatus(0, p.name))
          AppDSL.fromStore(StoreDSL.storeBacklogStatuses(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()


}
