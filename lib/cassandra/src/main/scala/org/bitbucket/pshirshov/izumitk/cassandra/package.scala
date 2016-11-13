package org.bitbucket.pshirshov.izumitk

import com.datastax.driver.core.PreparedStatement
import com.google.common.cache.LoadingCache

/**
  */
package object cassandra {
  type PSCache = LoadingCache[String, PreparedStatement]
}
