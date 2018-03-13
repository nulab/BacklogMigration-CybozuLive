package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.CybozuUser
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogUser

object UserConverter {

  def toBacklogUser(user: CybozuUser)(implicit ctx: MappingContext): Either[ConvertError, BacklogUser] =
    for {
      converted <- ctx.getUserName(user.key)
    } yield {
      BacklogUser(
        optId = None,
        optUserId = Some(converted), // mapping.dst
        optPassword = None,
        name = user.key, // mapping.src
        optMailAddress = None,
        roleType = BacklogConstantValue.USER_ROLE
      )
    }

  def toBacklogUser(maybeUser: Option[CybozuUser])(implicit ctx: MappingContext): Either[ConvertError, Option[BacklogUser]] =
    maybeUser match {
      case Some(user) => toBacklogUser(user) match {
        case Right(backlogUser) => Right(Some(backlogUser))
        case Left(error) => Left(error)
      }
      case None => Right(None)
    }
}
