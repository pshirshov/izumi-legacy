package org.bitbucket.pshirshov.izumitk.http.hal.model

import org.bitbucket.pshirshov.izumitk.hal.HalResource

@HalResource
case class HalFailure(
                       failureId: String
                       , failureType: String
                       , failureMessage: String
                       , stacktrace: Option[String]
                     )
