package com.nulabinc.backlog.c2b.services

import java.io.File

import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas._
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.dsl.AppDSL
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.exceptions.CybozuLiveImporterException
import com.nulabinc.backlog.c2b.interpreters.ConsoleDSL
import com.nulabinc.backlog.c2b.parsers.TextFileParser
import com.nulabinc.backlog.c2b.persistence.dsl._
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.readers.{CybozuCSVReader, CybozuTopicTextReader}
import com.osinka.i18n.Messages
import monix.reactive.Observable

object CybozuStore extends Logger {

  def copyToStore(exportedFiles: Array[File]): AppProgram[Unit] = {
    val todoFiles = {
      exportedFiles.filter(_.getName.contains("live_ToDoリスト_")) ++
      exportedFiles.filter(_.getName.contains("live_To-Do List_"))
    }
    val eventFiles = {
      exportedFiles.filter(_.getName.contains("live_イベント_")) ++
      exportedFiles.filter(_.getName.contains("live_Events_"))
    }
    val forumFiles = {
      exportedFiles.filter(_.getName.contains("live_掲示板_")) ++
      exportedFiles.filter(_.getName.contains("live_Forum_"))
    }
    val chatFiles = exportedFiles.filter(_.getName.endsWith(".txt"))

    for {
      _ <- todo(todoFiles)
      _ <- event(eventFiles)
      _ <- forum(forumFiles)
      _ <- chat(chatFiles)
    } yield ()
  }

  def todo(files: Array[File]): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.collect", Messages("issue.type.todo"))))
      _ <- AppDSL.fromStore(
        StoreDSL.writeDBStream(
          CybozuCSVReader.toCybozuTodo(files).map { result =>
            val creator = CybozuUser.from(result.issue.creator)
            val updater = CybozuUser.from(result.issue.updater)
            val assignees = result.issue.assignees.map(u => CybozuUser.from(u))
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
              _ <- comments(issueId, result.comments, TodoComment)
            } yield ()
          }
        )
      )
    } yield ()

  def event(files: Array[File]): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.collect", Messages("issue.type.event"))))
      _ <- AppDSL.fromStore(
        StoreDSL.writeDBStream(
          CybozuCSVReader.toCybozuEvent(files).map { result =>
            val creator = CybozuUser.from(result.issue.creator)
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
              _ <- comments(eventId, result.comments, EventComment)
            } yield ()
          }
        )
      )
    } yield ()

  def forum(files: Array[File]): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.collect", Messages("issue.type.forum"))))
      _ <- AppDSL.fromStore(
        StoreDSL.writeDBStream(
          CybozuCSVReader.toCybozuForum(files).map { result =>
            val creator = CybozuUser.from(result.issue.creator)
            val updater = CybozuUser.from(result.issue.updater)
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
              _ <- comments(forumId, result.comments, ForumComment)
            } yield ()
          }
        )
      )
    } yield ()

  def chat(files: Array[File]): AppProgram[Unit] = {
    import com.nulabinc.backlog.c2b.syntax.EitherOps._

    for {
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("message.init.collect", Messages("name.chat"))))
      _ <- AppDSL.fromStore(
        StoreDSL.writeDBStream(
          CybozuTopicTextReader.read(files).map { result =>
            val topic = TextFileParser.topic(result.topicText).orExit
            val postStream = result.comments.map(TextFileParser.post).map {
              case Right(value) => value
              case Left(error) => throw CybozuLiveImporterException(error.toString)
            }

            for {
              chatId <- StoreDSL.storeChat(CybozuDBChat.from(topic))
              _ <- StoreDSL.writeDBStream {
                val stream = postStream.map { post =>
                  val creator = CybozuUser.fromCybozuTextUser(post.postUser)
                  for {
                    postUserId <- insertOrUpdateUser(creator)
                    _ <- StoreDSL.storeComment(CybozuDBComment.from(chatId, post, postUserId), ChatComment)
                  } yield ()
                }
                Observable.fromIterator(stream.toIterator)
              }
            } yield ()
          }
        )
      )
    } yield ()
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

  private def insertOrUpdateUser(cybozuUser: CybozuUser): StoreProgram[AnyId] =
    for {
      optId <- StoreDSL.getCybozuUserByKey(cybozuUser.userId)
      id <- optId match {
        case Some(existUser) => StoreDSL.pure(existUser.id)
        case None => StoreDSL.storeCybozuUser(cybozuUser, Insert)
      }
    } yield id

  private def comments(issueId: AnyId, csvComments: Seq[CybozuCSVComment], commentType: CommentType): StoreProgram[Unit] = {
    val commentsPrograms = csvComments.map { comment =>
      val commentCreator = CybozuUser.from(comment.creator)
      for {
        creatorId <- insertOrUpdateUser(commentCreator)
      } yield CybozuDBComment.from(issueId, comment, creatorId)
    }
    for {
      comments <- sequential(commentsPrograms)
      _ <- StoreDSL.storeComments(comments, commentType)
    } yield ()
  }
}
