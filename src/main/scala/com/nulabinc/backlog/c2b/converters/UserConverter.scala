package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.{CybozuUser, MappingContext}
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogUser

class UserConverter()(implicit ctx: MappingContext) extends Converter[CybozuUser, BacklogUser] {

  def to(user: CybozuUser): Either[ConvertError, BacklogUser] =
    for {
      converted <- ctx.getUserName(user.value)
    } yield {
      BacklogUser(
        optId = None,
        optUserId = Some(converted), // mapping.dst
        optPassword = None,
        name = user.value, // mapping.src
        optMailAddress = None,
        roleType = BacklogConstantValue.USER_ROLE
      )
    }
}

