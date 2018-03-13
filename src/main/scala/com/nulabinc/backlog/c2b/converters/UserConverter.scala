package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.CybozuUser
import com.nulabinc.backlog.migration.common.conf.BacklogConstantValue
import com.nulabinc.backlog.migration.common.domain.BacklogUser

object UserConverter {

  def toBacklogUser(user: CybozuUser): BacklogUser =
    BacklogUser(
      optId = None,
      optUserId = Some(user.key), // TODO: mapping.dst
      optPassword = None,
      name = user.key,            // TODO: mapping.src
      optMailAddress = None,
      roleType = BacklogConstantValue.USER_ROLE
    )
}
