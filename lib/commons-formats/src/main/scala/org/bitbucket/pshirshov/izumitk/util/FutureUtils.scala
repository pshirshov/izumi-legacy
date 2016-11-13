package org.bitbucket.pshirshov.izumitk.util

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object FutureUtils {
  def futureToFutureTry[T](f: Future[T])
                          (implicit ec: ExecutionContext): Future[Try[T]] =
    f.map(Success(_))
      .recover { case x => Failure(x) }
}
