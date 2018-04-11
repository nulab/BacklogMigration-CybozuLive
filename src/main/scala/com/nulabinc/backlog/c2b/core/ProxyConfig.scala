package com.nulabinc.backlog.c2b.core

import java.net.InetSocketAddress

import akka.http.scaladsl.ClientTransport
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpCredentials}

object ProxyConfig {

  def create: Option[ClientTransport] = {
    val proxyUser = System.getProperty("http.proxyUser")
    val proxyPassword = System.getProperty("http.proxyPassword")
    val httpsProxyHost = System.getProperty("https.proxyHost")
    val httpsProxyPort = System.getProperty("https.proxyPort")
    val httpProxyHost = System.getProperty("http.proxyHost")
    val httpProxyPort = System.getProperty("http.proxyPort")

    val optAuth = createAuth(proxyUser, proxyPassword)
    val httpsProxyTransport = createProxyTransport(httpsProxyHost, httpsProxyPort, optAuth)
    val httpProxyTransport = createProxyTransport(httpProxyHost, httpProxyPort, optAuth)

    (httpsProxyTransport, httpProxyTransport) match {
      case (Some(https), _) => Some(https)
      case (None, Some(http)) => Some(http)
      case _ => None
    }
  }

  private[core] def createAuth(proxyUser: String, proxyPassword: String): Option[BasicHttpCredentials] =
    if (proxyUser != null && proxyPassword != null) {
      Some(headers.BasicHttpCredentials(proxyUser, proxyPassword))
    } else {
      None
    }

  private[core] def createProxyTransport(proxyHost: String,
                                         proxyPort: String,
                                         optProxyCredentials: Option[HttpCredentials]): Option[ClientTransport] =
    if (proxyHost != null && proxyPort != null) {
      try {
        val clientTransport = optProxyCredentials.map(credentials =>
          ClientTransport.httpsProxy(
            InetSocketAddress.createUnresolved(proxyHost, proxyPort.toInt),
            credentials
          )
        ).getOrElse(
          ClientTransport.httpsProxy(InetSocketAddress.createUnresolved(proxyHost, proxyPort.toInt))
        )
        Some(clientTransport)
      } catch {
        case _: Throwable => None
      }
    } else {
      None
    }
}
