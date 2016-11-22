package akka.http.scaladsl.server.directives

import akka.http.impl.util._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import com.fasterxml.jackson.databind.JsonMappingException

import scala.util.{Failure, Success}

trait JsonMarshallingDirectives extends MarshallingDirectives {

  import akka.http.scaladsl.server.directives.BasicDirectives._
  import akka.http.scaladsl.server.directives.FutureDirectives._
  import akka.http.scaladsl.server.directives.RouteDirectives._

  /**
    * Unmarshalls the requests entity to the given type passes it to its inner Route.
    * If there is a problem with unmarshalling the request is rejected with the [[akka.http.scaladsl.server.Rejection]]
    * produced by the unmarshaller.
    */
  def optionalEntity[T](um: FromRequestUnmarshaller[T]): Directive1[Option[T]] =
    extractRequestContext.flatMap[Tuple1[Option[T]]] { ctx ⇒
      import ctx.{executionContext, materializer}
      onComplete(um(ctx.request)) flatMap {
        case Success(value) ⇒ provide(Some(value))
        case Failure(Unmarshaller.NoContentException) ⇒ provide(None)
        case Failure(t: JsonMappingException) ⇒
          provide(None)
        case Failure(Unmarshaller.UnsupportedContentTypeException(x)) ⇒ reject(UnsupportedRequestContentTypeRejection(x))
        case Failure(x: IllegalArgumentException) ⇒ reject(ValidationRejection(x.getMessage.nullAsEmpty, Some(x)))
        case Failure(x) ⇒
          reject(MalformedRequestContentRejection(x.getMessage.nullAsEmpty, x.getCause))
      }
    } & cancelRejections(RequestEntityExpectedRejection.getClass, classOf[UnsupportedRequestContentTypeRejection])

}


