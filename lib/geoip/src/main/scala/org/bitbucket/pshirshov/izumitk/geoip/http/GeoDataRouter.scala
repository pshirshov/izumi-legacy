package org.bitbucket.pshirshov.izumitk.geoip.http

import akka.http.scaladsl.server.Directives.extractClientIP
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.geoip.models.GeoipResponse
import org.bitbucket.pshirshov.izumitk.geoip.services.GeoipService
import org.scalactic.{Bad, Good}

trait GeoDataRouter extends StrictLogging {

  protected def geoipService: GeoipService

  def withUserMaybeGeoData(router: => Option[GeoipResponse] => Route): Route =
    extractClientIP {
      ip =>
        router(geoipService.fetchGeoIPData(ip.toOption) match {
          case Good(geoOpt) => geoOpt
          case Bad(failures) =>
            logger.warn("Error while client's geo data retrieving")
            failures.map(f => logger.warn(f.message))
            None
        })
    }
}

