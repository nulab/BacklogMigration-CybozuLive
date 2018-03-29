package com.nulabinc.backlog.c2b.services

import backlog4s.apis.{PriorityApi, StatusApi, UserApi}
import com.nulabinc.backlog.c2b.datas.{BacklogPriority, BacklogStatus, BacklogUser}
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL
import monix.reactive.Observable

object BacklogToStore {

  import backlog4s.dsl.syntax._

  def user(api: UserApi): AppProgram[Unit] =
    for {
      users <- AppDSL.fromBacklog(api.all().orFail)
      _ <- AppDSL.consumeStream(
        Observable.fromIterator(users.iterator).map { user =>
          for {
            _ <- AppDSL.pure(user)
            _ <- AppDSL.fromDB(StoreDSL.storeBacklogUser(BacklogUser.from(user)))
          } yield ()
        }
      )
    } yield ()

  def priority(api: PriorityApi): AppProgram[Unit] =
    for {
      backlogPriorities <- AppDSL.fromBacklog(api.all)
      _ <- backlogPriorities match {
        case Right(data) =>
          val items = data.map(p => BacklogPriority(0, p.name))
          AppDSL.fromDB(StoreDSL.storeBacklogPriorities(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()

  def status(api: StatusApi): AppProgram[Unit] =
    for {
      backlogStatuses <- AppDSL.fromBacklog(api.all)
      _ <- backlogStatuses match {
        case Right(data) =>
          val items = data.map(p => BacklogStatus(0, p.name))
          AppDSL.fromDB(StoreDSL.storeBacklogStatuses(items))
        case Left(error) =>
          AppDSL.exit(error.toString, 1)
      }
    } yield ()


}
