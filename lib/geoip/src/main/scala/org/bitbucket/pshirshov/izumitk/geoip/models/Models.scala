package org.bitbucket.pshirshov.izumitk.geoip.models

import java.net.InetAddress

case class GeoipCountry(code: Option[String], name: Option[String])

case class GeoipLocation(latitude: Double, longitude: Double)

case class GeoipCity(name: Option[String], location: GeoipLocation, postalCode: Option[String])

case class GeoipResponse(ip: InetAddress, city: Option[GeoipCity], country: Option[GeoipCountry])
