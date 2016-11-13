package org.bitbucket.pshirshov.izumitk.app.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule


/**
  */
final class AppConstantsModule(val appId: String) extends ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  @Named("app.id")
  def appIdentifier: String = appId
}
