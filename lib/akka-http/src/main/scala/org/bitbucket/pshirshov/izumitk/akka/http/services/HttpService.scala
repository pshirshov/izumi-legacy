package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl._


trait HttpService {
  def routes: server.Route
}




