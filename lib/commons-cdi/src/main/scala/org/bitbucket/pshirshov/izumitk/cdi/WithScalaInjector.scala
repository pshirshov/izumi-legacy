package org.bitbucket.pshirshov.izumitk.cdi

import com.google.inject.Injector
import net.codingwell.scalaguice.InjectorExtensions

trait WithScalaInjector {
  // just to avoid implicit conversions in inherited classes
  protected implicit class VisibleScalaInjector(injector: Injector)
    extends InjectorExtensions.ScalaInjector(injector)
}
