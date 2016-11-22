package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, RequestContext, RouteResult}
import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{IntNode, JsonNodeFactory, ObjectNode, TextNode}
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.util.RequestTransformer
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.services.{FailureRecord, FailureRepository, ServiceFailure}
import org.bitbucket.pshirshov.izumitk.util.TimeUtils
import org.scalactic.{Bad, Every, Good, Or}

import scala.concurrent.Future
import scala.util.control.NonFatal



@Singleton
class DefaultJsonAPIPolicy @Inject()
(
  failureRepository: FailureRepository
  , override val protocol: SerializationProtocol
  , protected val metrics: MetricRegistry
  , @Named("headers.cors") corsHeaders: Seq[RawHeader]
  , @Named("standardMapper") protected val exceptionMapper: JacksonMapper
  , @Named("app.id") protected val productId: String
) extends JsonAPIPolicy
  with RequestTransformer
  with StrictLogging {

  import JsonAPI._

  private val expPrefix = s"$productId-ncrt"

  override def rejectionHandler(): RejectionHandler = {
    RejectionHandler.newBuilder()
      .handle {
        case r =>
          complete((Forbidden, s"REJECTED: $r"))
      }
      .result()
  }

  override def exceptionHandler(): ExceptionHandler = {
    ExceptionHandler {
      case NonFatal(e) => ctx =>
        logger.error(s"Critical failure while handling request:\n${ctx.request}")
        val failureId = failureRepository.recordFailure(FailureRecord(e))
        val response = JsonNodeFactory.instance.objectNode()
        response.set("code", new IntNode(JsonAPI.Codes.INTERNAL_FAILURE))
        response.set("data", JsonNodeFactory.instance.objectNode())
        val meta: ObjectNode = JsonNodeFactory.instance.objectNode()
        meta.set("failureId", new TextNode(failureId))
        meta.set("failureType", new TextNode("service"))
        meta.set("message", new TextNode("Critical failure during request processing. Request was not logged."))
        response.set("meta", meta)
        val entity: ResponseEntity = exceptionMapper.writeValueAsString(response)
        ctx.complete(HttpResponse(InternalServerError, entity = entity.withContentType(ContentTypes.`application/json`)))
    }
  }

  def CORSOptions: (RequestContext) => Future[RouteResult] = {
    ctx: RequestContext =>
      ctx
        .mapRequest(r => r.copy(headers = corsHeaders.to[collection.immutable.Seq] ++ r.headers))
        .complete(StatusCodes.OK)
  }

  def formatResponse[R: Manifest](transformedOutput: Or[Response, Every[ServiceFailure]]): (JsonNode, collection.immutable.Seq[HttpHeader]) = {
    val factory = JsonNodeFactory.instance
    val result = factory.objectNode()
    val meta = factory.objectNode()

    val (data, headers, code) = transformedOutput match {
      case Good(r) =>
        val serialized = r.body match {
          case v: JsonNode =>
            v
          case o =>
            protocol.protocolMapper.valueToTree[JsonNode](o)
        }
        (serialized, r.headers, JsonAPI.Codes.OK)

      case Bad(r: Every[ServiceFailure]) =>
        val code = createErrorResponse(meta, r)
        (JsonNodeFactory.instance.objectNode(), Seq[HttpHeader](), code)
    }

    meta.set("serverTime", new TextNode(TimeUtils.isoNow))
    result.set("meta", meta)
    result.set("data", data)
    result.set("code", new IntNode(code))
    (result, headers.to[collection.immutable.Seq] ++ corsHeaders)
  }

  private def createErrorResponse[R: Manifest](meta: ObjectNode, r: Every[ServiceFailure]): Int = {
    val problems = r.toSeq


    val (exceptions, justProblems) = problems
      .partition(t => t.isInstanceOf[Throwable] && !t.isInstanceOf[ControlException])

    val exceptionThrowables = exceptions.map(_.asInstanceOf[Throwable])

    val code = getFailureCode(problems, exceptionThrowables)
    import scala.collection.JavaConverters._

    val failuresNode = JsonNodeFactory.instance.arrayNode()
    failuresNode.addAll(problems.map(formatFailure).asJava)
    meta.set("failures", failuresNode)

    if (exceptions.nonEmpty) {
      val failureId = failureRepository.recordFailure(FailureRecord(exceptionThrowables))
      meta.set("failureId", new TextNode(failureId))
    }

    metrics.counter(expPrefix).inc(problems.size)

    problems.foreach {
      p =>
        metrics.counter(s"$expPrefix-${p.getClass.getCanonicalName}").inc()
    }

    code
  }

  private def getFailureCode(problems: Seq[ServiceFailure], exceptions: Seq[Throwable]): Int = {
    if (exceptions.nonEmpty) {
      JsonAPI.Codes.INTERNAL_FAILURE
    } else if (problems.exists(_.isInstanceOf[InternalFailureException])) {
      JsonAPI.Codes.INTERNAL_FAILURE
    } else if (problems.exists(_.isInstanceOf[ForbiddenException])) {
      JsonAPI.Codes.PERMISSION_DENIED
    } else if (problems.exists(_.isInstanceOf[NotFoundException])) {
      JsonAPI.Codes.NOT_FOUND
    } else if (problems.exists(_.isInstanceOf[IllegalRequestException])) {
      JsonAPI.Codes.MALFORMED_REQUEST
    } else if (problems.exists(_.isInstanceOf[InvalidVersionException])) {
      JsonAPI.Codes.INVALID_VERSION
    } else {
      JsonAPI.Codes.INTERNAL_FAILURE
    }
  }

  private def formatFailure(t: ServiceFailure): JsonNode = {
    val failure = JsonNodeFactory.instance.objectNode()
    failure.set("class", new TextNode(t.getClass.getCanonicalName))

    t match {
      case throwable: Throwable =>
        failure.set("message", new TextNode(throwable.getMessage))
      case s: ServiceFailure=>
        failure.set("message", new TextNode(s.message))
      case _ =>
    }

    t match {
      case ce: ControlException =>
        failure.set("failureType", new TextNode("control"))

      case ife: InternalFailureException =>
        failure.set("failureType", new TextNode("catchedInternal"))

      case throwable: Throwable =>
        failure.set("failureType", new TextNode("unexpectedInternal"))

      case s: ServiceFailure=>
        failure.set("failureType", new TextNode("serviceFailure"))

      case _ =>
        failure.set("failureType", new TextNode("unknown"))
    }

    failure
  }
}
