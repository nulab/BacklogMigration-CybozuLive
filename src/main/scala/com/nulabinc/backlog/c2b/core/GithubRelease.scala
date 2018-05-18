package com.nulabinc.backlog.c2b.core

import java.io._
import java.net._

import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.util.Try

object GithubRelease {

  private val url = new URL("https://api.github.com/repos/nulab/BacklogMigration-CybozuLive/releases")
  private val http = url.openConnection().asInstanceOf[HttpURLConnection]

  def getLatestVersion(): Try[String] = {
    val optProxyUser = Option(System.getProperty("https.proxyUser"))
    val optProxyPass = Option(System.getProperty("https.proxyPassword"))

    (optProxyUser, optProxyPass) match {
      case (Some(proxyUser), Some(proxyPass)) =>
        Authenticator.setDefault(new Authenticator() {
          override def getPasswordAuthentication: PasswordAuthentication = {
            new PasswordAuthentication(proxyUser, proxyPass.toCharArray)
          }
        })
      case _ => ()
    }

    Try {
      http.setRequestMethod("GET")
      http.connect()

      val reader = new BufferedReader(new InputStreamReader(http.getInputStream))
      val output = readLines(reader)
      reader.close()

      output.parseJson match {
        case JsArray(releases) if releases.nonEmpty =>
          releases(0).asJsObject.fields.apply("tag_name").convertTo[String].replace("v", "")
        case _ => ""
      }
    }
  }

  private def readLines(reader: BufferedReader): String = {
    val output = new StringBuilder()
    var line = ""

    while (line != null) {
      line = reader.readLine()
      if (line != null)
        output.append(line)
    }
    output.toString()
  }
}
