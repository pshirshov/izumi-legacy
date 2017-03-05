package org.bitbucket.pshirshov.izumitk.app.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId


/**
  */
final class AppConstantsModule(val appId: AppId) extends ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  @Named("app.id")
  def appIdentifier: AppId = appId
}
