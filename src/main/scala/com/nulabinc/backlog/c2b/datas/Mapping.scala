package com.nulabinc.backlog.c2b.datas

import java.nio.file.{Path, Paths}

import com.nulabinc.backlog.c2b.converters.{ConvertError, MappingFiled}
import com.nulabinc.backlog.migration.common.domain.BacklogUser

import scala.collection.immutable.HashMap

sealed trait Mapping[S, D]

object Mapping {
  val userMappingFilePath: Path = Paths.get("./mapping/users.csv")
  val statusMappingFilePath: Path = Paths.get("./mapping/satuses.csv")
  val priorityMappingFilePath: Path = Paths.get("./mapping/priorities.csv")
}

case class UserMapping(source: String, destination: String) extends Mapping[CybozuUser, BacklogUser]
case class StatusMapping(source: String, destination: String) extends Mapping[CybozuStatus, String]
case class PriorityMapping(source: String, destination: String) extends Mapping[CybozuPriority, String]

case class MappingContext(
   userMappings: HashMap[String, UserMapping],
   statusMappings: HashMap[String, StatusMapping],
   priorityMappings: HashMap[String, PriorityMapping]
) {

  def getUserName(source: String): Either[ConvertError, String] =
    userMappings.get(source)
      .map(mapping => Right(mapping.destination))
      .getOrElse(Left(MappingFiled[CybozuUser](source)))

  def getStatusName(source: CybozuStatus): Either[ConvertError, String] =
    statusMappings.get(source.value)
      .map(mapping => Right(mapping.destination))
      .getOrElse(Left(MappingFiled[CybozuStatus](source.value)))

  def getPriorityName(source: CybozuPriority): Either[ConvertError, String] =
    statusMappings.get(source.value)
      .map(mapping => Right(mapping.destination))
      .getOrElse(Left(MappingFiled[CybozuPriority](source.value)))

}