package org.bitbucket.pshirshov.izumitk.cdi

import com.google.inject.Injector


trait WithInjector {
  protected def injector: Injector
}
