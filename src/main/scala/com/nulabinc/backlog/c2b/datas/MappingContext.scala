package com.nulabinc.backlog.c2b.datas

import com.nulabinc.backlog.c2b.converters.{ConvertError, MappingFail}

import scala.collection.immutable.HashMap


case class MappingContext(
   userMappings: HashMap[String, String],
   statusMappings: HashMap[String, String],
   priorityMappings: HashMap[String, String]
) {

  def getUserName(source: String): Either[ConvertError, String] =
    userMappings.get(source)
      .map(dst => Right(dst))
      .getOrElse(Left(MappingFail[CybozuDBUser](source)))

  def getStatusName(source: CybozuDBStatus): Either[ConvertError, String] =
    statusMappings.get(source.value)
      .map(dst => Right(dst))
      .getOrElse(Left(MappingFail[CybozuDBStatus](source.value)))

  def getPriorityName(source: CybozuDBPriority): Either[ConvertError, String] =
    statusMappings.get(source.value)
      .map(dst => Right(dst))
      .getOrElse(Left(MappingFail[CybozuDBPriority](source.value)))

}