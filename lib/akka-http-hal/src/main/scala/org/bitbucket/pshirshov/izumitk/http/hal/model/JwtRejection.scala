package org.bitbucket.pshirshov.izumitk.http.hal.model

import akka.http.javadsl.server.CustomRejection

case class JwtRejection(token: String) extends CustomRejection
