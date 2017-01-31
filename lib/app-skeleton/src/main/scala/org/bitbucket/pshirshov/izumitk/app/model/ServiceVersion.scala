package org.bitbucket.pshirshov.izumitk.app.model

import org.bitbucket.pshirshov.izumitk.app.Version

case class ServiceVersion(version: String
                          , revision: String
                          , timestamp: String
                          , author: String
                         ) {
  def this() = this(Version.getVersion, Version.getRevision, Version.getBuildTimestamp, Version.getBuildUser)
}
