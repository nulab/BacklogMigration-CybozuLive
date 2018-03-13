package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.{CybozuPriority, CybozuStatus, CybozuUser}

case class Mapping(source: String, destination: String)

case class MappingContext(
  userMappings: IndexedSeq[Mapping],
  statusMappings: IndexedSeq[Mapping],
  priorityMappings: IndexedSeq[Mapping]
) {

  def getUserName(source: String): Either[ConvertError, String] =
    userMappings.find(_.source == source) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[CybozuUser](source))
    }

  def getStatusName(source: CybozuStatus): Either[ConvertError, String] =
    statusMappings.find(_.source == source.value) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[CybozuStatus](source.value))
    }

  def getPriorityName(source: CybozuPriority): Either[ConvertError, String] =
    statusMappings.find(_.source == source.value) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[CybozuPriority](source.value))
    }
}
