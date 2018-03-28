package com.nulabinc.backlog.c2b.core

import java.security.cert.X509Certificate

import javax.net.ssl._

import scala.util.{Failure, Success, Try}

object DisableSSLCertificateChecker extends Logger {

  def check() = Try{
    try {
      val context: SSLContext = SSLContext.getInstance("TLS")
      val trustManagerArray: Array[TrustManager] = Array(
        new NullX509TrustManager()
      )
      context.init(null, trustManagerArray, null)
      HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory)
      HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier())
      Success
    } catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        Failure(e)
    }
  }

  private[this] class NullX509TrustManager extends X509TrustManager {
    override def checkClientTrusted(chain: Array[X509Certificate], authType: String) {}
    override def checkServerTrusted(chain: Array[X509Certificate], authType: String) {}
    override def getAcceptedIssuers: Array[X509Certificate] = Array.ofDim[X509Certificate](0)
  }

  private[this] class NullHostnameVerifier extends HostnameVerifier {
    override def verify(hostname: String, session: SSLSession): Boolean = true
  }
}
