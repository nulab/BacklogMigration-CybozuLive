package com.nulabinc.backlog.c2b.services

import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{Insert, StoreDSL}
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.readers.ReadResult
import monix.reactive.Observable

object CSVtoStore {

  def todo(observable: Observable[ReadResult[CybozuCSVTodo]]): AppProgram[Unit] = {

    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuDBUser.from(result.issue.creator)
          val updater = CybozuDBUser.from(result.issue.updater)
          val assignees = result.issue.assignees.map(u => CybozuDBUser.from(u))
          for {
            // Save issues
            creatorId <- insertOrUpdateUser(creator)
            updaterId <- insertOrUpdateUser(updater)
            issueId <- {
              val issue = CybozuDBTodo.from(
                todo = result.issue,
                creatorId = creatorId,
                updaterId = updaterId
              )
              StoreDSL.storeTodo(issue)
            }
            // Save assignees
            assigneeIdsProgram = assignees.map(insertOrUpdateUser)
            assigneeIds <- sequential(assigneeIdsProgram)
            _ <- StoreDSL.storeTodoAssignees(issueId, assigneeIds)
            // Save comments
            _ <- comments(issueId, result.comments)
          } yield ()
        }
      )
    } yield ()

    AppDSL.fromDB(dbProgram)
  }

  def event(observable: Observable[ReadResult[CybozuCSVEvent]]): AppProgram[Unit] = {
    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuDBUser.from(result.issue.creator)
          for {
            // Save event
            creatorId <- insertOrUpdateUser(creator)
            eventId <- {
              val issue = CybozuDBEvent.from(
                event = result.issue,
                creatorId = creatorId
              )
              StoreDSL.storeEvent(issue)
            }
            // Save comments
            _ <- comments(eventId, result.comments)
          } yield ()
        }
      )
    } yield ()
    AppDSL.fromDB(dbProgram)
  }

  def forum(observable: Observable[ReadResult[CybozuCSVForum]]): AppProgram[Unit] = {
    val dbProgram = for {
      _ <- StoreDSL.writeDBStream(
        observable.map { result =>
          val creator = CybozuDBUser.from(result.issue.creator)
          val updater = CybozuDBUser.from(result.issue.updater)
          for {
            // Save event
            creatorId <- insertOrUpdateUser(creator)
            updaterId <- insertOrUpdateUser(updater)
            forumId <- {
              val forum = CybozuDBForum.from(
                forum = result.issue,
                creatorId = creatorId,
                updaterId = updaterId
              )
              StoreDSL.storeForum(forum)
            }
            // Save comments
            _ <- comments(forumId, result.comments)
          } yield ()
        }
      )
    } yield ()
    AppDSL.fromDB(dbProgram)
  }

  private def sequential[A](prgs: Seq[StoreProgram[A]]): StoreProgram[Seq[A]] =
    prgs.foldLeft(StoreDSL.pure(Seq.empty[A])) {
      case (newPrg, prg) =>
        newPrg.flatMap { results =>
          prg.map { result =>
            results :+ result
          }
        }
    }

  private def insertOrUpdateUser(cybozuUser: CybozuDBUser): StoreProgram[AnyId] =
    for {
      optId <- StoreDSL.getCybozuUserByKey(cybozuUser.userId)
      id <- optId match {
        case Some(existUser) => StoreDSL.pure(existUser.id)
        case None => StoreDSL.storeCybozuUser(cybozuUser, Insert)
      }
    } yield id

  private def comments(issueId: AnyId, csvComments: Seq[CybozuCSVComment]): StoreProgram[Unit] = {
    val commentsPrograms = csvComments.map { comment =>
      val commentCreator = CybozuDBUser.from(comment.creator)
      for {
        creatorId <- insertOrUpdateUser(commentCreator)
      } yield CybozuDBComment.from(issueId, comment, creatorId)
    }
    for {
      comments <- sequential(commentsPrograms)
      _ <- StoreDSL.storeTodoComments(comments)
    } yield ()
  }
}
