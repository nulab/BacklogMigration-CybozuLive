package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.CybozuUser
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogUser

object UserConverter {

  def toBacklogUser(user: CybozuUser)(implicit ctx: MappingContext): Either[ConvertError[CybozuUser], BacklogUser] =
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
}
