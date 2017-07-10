package org.bitbucket.pshirshov.izumitk.geoip.services

import java.io._
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

import com.google.inject.Inject
import com.google.inject.name.Named
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveInputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.bitbucket.pshirshov.izumitk.failures.model.Maybe
import org.bitbucket.pshirshov.izumitk.failures.util.maybe.TryExtensions
import org.bitbucket.pshirshov.izumitk.geoip.models.{GeoipCity, GeoipCountry, GeoipLocation, GeoipResponse}
import resource._

import scala.util.Try

class GeoipServiceImpl @Inject()
(
  @Named("@geodata.db-country-url") protected val countryDBurl: String
  , @Named("@geodata.db-city-url") protected val cityDBurl: String
) extends GeoipService with StrictLogging {

  private val geoipCountry: AtomicReference[DatabaseReader] = new AtomicReference()
  private val geoipCity: AtomicReference[DatabaseReader] = new AtomicReference()

  this.geoipCountry.set(loadDb(countryDBurl, "GeoLite2-Country.mmdb").orNull)
  this.geoipCity.set(loadDb(cityDBurl, "GeoLite2-City.mmdb").orNull)

  private def loadDb(url: String, expected: String): Option[DatabaseReader] = {
    try {
      val geoipCache: File = downloadGeoIp(url, expected)
      val db = new DatabaseReader.Builder(geoipCache).withCache(new CHMCache()).build()
      logger.info(s"Loaded geoip database ${geoipCache.getCanonicalPath}: ${db.getMetadata}")
      Some(db)
    } catch {
      case e: Throwable =>
        logger.error("Cannot download geoipCountry database", e)
        None
    }
  }

  private def downloadGeoIp(url: String, expected: String): File = {
    val geoipBase = FileUtils.getTempDirectory.toPath.resolve("geolite.db")
    val geoipCache = geoipBase.toFile
    geoipCache.mkdirs()
    val geoipFile = geoipBase.resolve(expected).toFile
    if (!geoipFile.exists()) {
      logger.info("GeoIP db does not exist")
      val client = HttpClients.createDefault()
      val archive = geoipBase
        .resolve(s"geoip-$expected-${
          System.nanoTime()
        }.tar.gz")
        .toFile
      for (response <- managed(client.execute(new HttpGet(url)))) {
        val entity = response.getEntity
        if (entity != null) {
          for (outstream <- managed(new FileOutputStream(archive, false)))
            entity.writeTo(outstream)
        }
      }
      val cacheFile: Option[File] = GeoipServiceImpl.uncompressGeoipDb(archive, expected, geoipCache)

      cacheFile match {
        case Some(f) =>
          FileUtils.moveFile(f, geoipFile)
        case _ =>
          throw new IllegalStateException(
            String.format(
              "GeoIP database was not found in downloaded archive: %s",
              geoipCache))
      }
    }
    geoipFile
  }

  override def fetchGeoIPData(ip: Option[InetAddress]): Maybe[Option[GeoipResponse]] = {
    Try {
      ip.map {
        ipAddress =>
          val country = Option(geoipCountry.get()).map {
            db =>
              val response = db.country(ipAddress)
              GeoipCountry(Option(response.getCountry.getIsoCode), Option(response.getCountry.getName))
          }

          val city = Option(geoipCity.get()).map {
            db =>
              val response = db.city(ipAddress)
              GeoipCity(
                Option(response.getCity.getName)
                , GeoipLocation(response.getLocation.getLatitude, response.getLocation.getLongitude)
                , Option(response.getPostal.getCode)
              )
          }
          GeoipResponse(ipAddress, city, country)
      }
    }.maybe
  }
}

object GeoipServiceImpl extends StrictLogging {

  private def uncompressGeoipDb(tarFile: File, expected: String, dest: File): Option[File] = {
    dest.mkdir()
    for {
      fis <- managed(new FileInputStream(tarFile))
      bis <- managed(new BufferedInputStream(fis))
      gzis <- managed(new GzipCompressorInputStream(bis))
      tais <- managed(new TarArchiveInputStream(gzis))
    } {
      var tarEntry: TarArchiveEntry = tais.getNextTarEntry
      while (tarEntry != null) {
        val destPath: File = new File(dest, tarEntry.getName)
        logger.debug("GeoIP arhive entry: {}", destPath.getCanonicalPath)
        if (tarEntry.isDirectory) {
          destPath.mkdirs()
        } else {
          if (tarEntry.getName.endsWith(expected)) {
            destPath.createNewFile()
            val btoRead: Array[Byte] = Array.ofDim[Byte](1024)
            for (bout <- managed(
              new BufferedOutputStream(new FileOutputStream(destPath)))) {
              var len: Int = 0
              len = tais.read(btoRead)
              while (len != -1) {
                bout.write(btoRead, 0, len)
                len = tais.read(btoRead)
              }
            }
            return Some(destPath)
          }
        }
        tarEntry = tais.getNextTarEntry
      }
    }
    None
  }

}
