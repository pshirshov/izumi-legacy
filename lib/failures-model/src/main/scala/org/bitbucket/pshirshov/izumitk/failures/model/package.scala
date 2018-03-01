package org.bitbucket.pshirshov.izumitk.failures

import org.scalactic.{Every, Or}

package object model {
  type Maybe[+T] = Or[T, Every[ServiceFailure]]
}
