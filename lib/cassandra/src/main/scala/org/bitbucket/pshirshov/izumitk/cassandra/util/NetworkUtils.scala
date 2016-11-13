package org.bitbucket.pshirshov.izumitk.cassandra.util

import java.net.InetSocketAddress
import java.util.regex.{Matcher, Pattern}

import com.datastax.driver.core.ProtocolOptions

/**
  */
object NetworkUtils {
  // TODO: more scala-ish
  private val IPV6_PATTERN = Pattern.compile("^\\[([:a-fA-F0-9]+)\\](:(\\d+))?$")
  private val IPV4_PATTERN = Pattern.compile("^([\\.0-9]+)(:(\\d+))?$")
  private val ENDPOINT_TOKENS_PATTERN: Pattern = Pattern.compile(":")

  def getAddress(endpoint: String): InetSocketAddress = {
    val address = endpoint.trim()
    val ipv6matcher = IPV6_PATTERN.matcher(address)
    val ipv4matcher = IPV4_PATTERN.matcher(address)

    if (ipv6matcher.matches()) {
      return getAddress(ipv6matcher)
    } else if (ipv4matcher.matches()) {
      return getAddress(ipv4matcher)
    } else {
      val tokens = ENDPOINT_TOKENS_PATTERN.split(endpoint)
      if (tokens.length == 1) {
        return new InetSocketAddress(tokens(0), ProtocolOptions.DEFAULT_PORT)
      } else if (tokens.length == 2) {
        return new InetSocketAddress(tokens(0), java.lang.Integer.parseInt(tokens(1)))
      }
    }
    throw new IllegalArgumentException("Unable to parse address " + address)
  }

  private def getAddress(addressMatcher: Matcher): InetSocketAddress = {
    val rawPort = addressMatcher.group(3)
    val port = if (rawPort == null) {
      ProtocolOptions.DEFAULT_PORT
    } else {
      java.lang.Integer.parseInt(rawPort)
    }

    InetSocketAddress.createUnresolved(addressMatcher.group(1), port)
  }
}
