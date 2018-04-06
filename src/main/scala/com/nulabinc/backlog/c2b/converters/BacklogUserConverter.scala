package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.{CybozuDBUser, MappingContext}
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogUser

class BacklogUserConverter()(implicit ctx: MappingContext) extends Converter[CybozuDBUser, BacklogUser] {

  def to(user: CybozuDBUser): Either[ConvertError, BacklogUser] =
    for {
      converted <- ctx.getUserName(user.userId)
    } yield {
      BacklogUser(
        optId = None,
        optUserId = Some(converted), // mapping.dst
        optPassword = None,
        name = user.userId, // mapping.src
        optMailAddress = None,
        roleType = BacklogConstantValue.USER_ROLE
      )
    }
}

