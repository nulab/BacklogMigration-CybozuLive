package com.nulabinc.backlog.c2b.core

import spray.json._
import spray.json.DefaultJsonProtocol._

object GithubRelease {

  val url: String = "https://api.github.com/repos/nulab/BacklogMigration-CybozuLive/releases"

  def parseLatestVersion(source: String): String =
    parseToJsonArray(source)
      .flatMap { releases =>
        releases
          .map(jsValue => extractTagName(jsValue.asJsObject))
          .map(_.replace("v", ""))
          .headOption
      }
      .getOrElse("")

  private[core] def parseToJsonArray(str: String): Option[Vector[JsValue]] =
    str.parseJson match {
      case JsArray(values) => Some(values)
      case _ => None
    }

  private[core] def extractTagName(jsObject: JsObject): String =
    jsObject.fields.apply("tag_name").convertTo[String]

}
