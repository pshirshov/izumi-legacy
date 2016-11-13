package org.bitbucket.pshirshov.izumitk.app.modules

import javax.servlet.Servlet

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.eclipse.jetty.server.handler.{DefaultHandler, HandlerCollection}
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.ServletContextHandler

import scala.collection.immutable
import scala.language.existentials

case class ServletBinding(pathSpec: String, clazz: Class[_ <: Servlet])


final class JettyModule() extends ScalaModule {
  def configure(): Unit = {}

  @Provides
  @Singleton
  def jettyServer(@Named("@jetty.interface") host: String
                  , @Named("@jetty.port") port: Int
                  , bindings: immutable.Set[ServletBinding]
                 ): Server = {
    val server = new Server()
    val connector = new ServerConnector(server)
    connector.setPort(port)
    connector.setHost(host)
    server.setConnectors(Array(connector))

    val handler = new ServletContextHandler()
    handler.setContextPath("/")
    bindings.foreach {
      b =>
        handler.addServlet(b.clazz, b.pathSpec)
    }

    val handlers = new HandlerCollection()
    handlers.setHandlers(Array(handler, new DefaultHandler()))
    server.setHandler(handlers)
    server
  }


}


