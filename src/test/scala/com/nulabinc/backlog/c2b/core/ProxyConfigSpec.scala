package com.nulabinc.backlog.c2b.core

import org.scalatest.{FlatSpec, Matchers}

class ProxyConfigSpec extends FlatSpec with Matchers {

  "ProxyConfig.createAuth" should "return None" in {
    val actual1 = ProxyConfig.createAuth(null, "password")
    val actual2 = ProxyConfig.createAuth("user", null)
    actual1 shouldEqual None
    actual2 shouldEqual None
  }

  "ProxyConfig.createAuth" should "return Some" in {
    val actual = ProxyConfig.createAuth("user", "password")
    actual.isDefined shouldEqual true
  }

  "ProxyConfig.createProxyTransport" should "return None" in {
    val actual1 = ProxyConfig.createProxyTransport(null, "port", None)
    val actual2 = ProxyConfig.createProxyTransport("host", null, None)
    actual1 shouldEqual None
    actual2 shouldEqual None
  }

  "ProxyConfig.createProxyTransport" should "return Some" in {
    val actual = ProxyConfig.createProxyTransport("host", "8878", None)
    actual.isDefined shouldEqual true
  }

}
