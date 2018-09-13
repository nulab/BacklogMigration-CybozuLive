package com.nulabinc.backlog.c2b.core

import com.nulabinc.backlog.c2b.dsl.{AppDSL, HttpDSL}
import com.nulabinc.backlog.c2b.dsl.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.dsl.HttpADT.Response
import spray.json._
import spray.json.DefaultJsonProtocol._

object GithubRelease {

  private val url = "https://api.github.com/repos/nulab/BacklogMigration-CybozuLive/releases"

  def getLatestVersionProgram: AppProgram[Response[String]] =
    for {
      result <- AppDSL.fromHttp(HttpDSL.get(url))
      response = result match {
        case Right(str) =>
          Right(
            parseToJsonArray(str)
              .flatMap { releases =>
                releases
                  .map(jsValue => extractTagName(jsValue.asJsObject))
                  .map(_.replace("v", ""))
                  .headOption
              }
              .getOrElse("")
          )
        case Left(error) =>
          Left(error)
      }
    } yield response

  private[core] def parseToJsonArray(str: String): Option[Vector[JsValue]] =
    str.parseJson match {
      case JsArray(values) => Some(values)
      case _ => None
    }

  private[core] def extractTagName(jsObject: JsObject): String =
    jsObject.fields.apply("tag_name").convertTo[String]

}
