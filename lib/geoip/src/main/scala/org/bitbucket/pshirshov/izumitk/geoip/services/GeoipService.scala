package org.bitbucket.pshirshov.izumitk.geoip.services

import java.net.InetAddress

import org.bitbucket.pshirshov.izumitk.failures.model.Maybe
import org.bitbucket.pshirshov.izumitk.geoip.models.GeoipResponse

trait GeoipService {
  def fetchGeoIPData(ip: Option[InetAddress]): Maybe[Option[GeoipResponse]]
}
