package org.bitbucket.pshirshov.izumitk.http.modules

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}


final class AkkaModule() extends ScalaModule {
  override def configure(): Unit = {}

  // To ensure config presence
  @Provides
  @Singleton
  @Named("akka.config")
  def akkaConfig(@Named("@akka.*") c: Config): Config = {
    c
  }

  @Provides
  @Singleton
  def actorSystem(@Named("app.config") appConfig: Config): ActorSystem = {
    ActorSystem("akka", appConfig)
  }

  @Provides
  @Singleton
  def dispatcher(implicit system: ActorSystem): ExecutionContextExecutor = system.dispatcher

  @Provides
  @Singleton
  def actorMaterializer(implicit system: ActorSystem): ActorMaterializer = ActorMaterializer()

  @Provides
  @Singleton
  def materializer(materializer: ActorMaterializer): Materializer = materializer

  @Provides
  @Singleton
  def executionContext(): ExecutionContext = ExecutionContext.global
}
