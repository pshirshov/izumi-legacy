package org.bitbucket.pshirshov.izumitk.failures.services

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import com.typesafe.scalalogging.StrictLogging
import resource._

@Singleton
class FailuresDump @Inject()(
                            failureRepository: FailureRepository
                            , @Named("typingMapper") mapper: JacksonMapper
                            , @Named("app.id") appName: String
                            ) extends StrictLogging {
  def dump(): Unit = {
    val m = new ObjectMapper(mapper) {} // to avoid side effects
    m.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
    m.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
    m.configure(SerializationFeature.INDENT_OUTPUT, true)

//    falureRepository.recordFailure(FailureRecord(Map("test" -> "xxx"), Vector(new RuntimeException())))

    val target = Paths.get(s"failures-$appName-${System.currentTimeMillis()}.zip")
    managed(new ZipOutputStream(new FileOutputStream(target.toFile))) foreach {
      out =>
        logger.info(s"Dumping into $target...")

        failureRepository.enumerate {
          record =>
            val fname = record.id.replace(":", "-")
            val e = new ZipEntry(s"failure-$fname.json")
            out.putNextEntry(e)
            m.writeValue(out, record)
            out.closeEntry()
        }

        logger.info("DONE")

    }

  }
}
