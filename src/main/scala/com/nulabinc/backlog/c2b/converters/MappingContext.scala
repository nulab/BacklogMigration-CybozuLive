package com.nulabinc.backlog.c2b.converters

import com.nulabinc.backlog.c2b.datas.CybozuUser

case class Mapping(source: String, destination: String)

case class MappingContext(
  userMappings: IndexedSeq[Mapping],
  statusMappings: IndexedSeq[Mapping],
  priorityMappings: IndexedSeq[Mapping]
) {

  def getUserName(source: String): Either[ConvertError[CybozuUser], String] =
    userMappings.find(_.source == source) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[CybozuUser](source))
    }

  // TODO: String -> CybozuStatus
  def getStatusName(source: String): Either[ConvertError[String], String] =
    statusMappings.find(_.source == source) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[String](source))
    }

  // TODO: String -> CybozuPriority
  def getPriorityName(source: String): Either[ConvertError[String], String] =
    statusMappings.find(_.source == source) match {
      case Some(mapping) => Right(mapping.destination)
      case None => Left(MappingFiled[String](source))
    }
}
