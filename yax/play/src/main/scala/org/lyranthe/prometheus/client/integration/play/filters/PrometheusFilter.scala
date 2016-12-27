package org.lyranthe.prometheus.client.integration.play.filters

import com.google.inject.{Inject, Singleton}
#+play25
import akka.stream.Materializer
#-play25
import org.lyranthe.prometheus.client._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

private case class RouteDetails(method: String, route: String)

@Singleton
class PrometheusFilter @Inject()(implicit
#+play25
  val mat: Materializer,
#-play25
  registry: Registry,
  executionContext: ExecutionContext
) extends Filter {
  private final val ServerErrorClass = "5xx"

  private val httpHistogramBuckets = {
    val buckets = for (p <- Vector[Double](0.0001, 0.001, 0.01, 0.1, 1, 10);
                       s <- Vector(1, 2, 5)) yield (p * s)
    HistogramBuckets(buckets: _*)
  }

  private val httpRequestLatency =
    Histogram(metric"http_request_duration_seconds",
              "Duration of HTTP request in seconds")(httpHistogramBuckets)
      .labels(label"method", label"path", label"status")
      .register

  private val httpRequestMismatch =
    Counter(metric"http_request_mismatch_total", "Number mismatched routes")
      .labels()
      .register

  def apply(nextFilter: RequestHeader => Future[Result])(
      requestHeader: RequestHeader): Future[Result] = {
    val timer  = Timer()
    val future = nextFilter(requestHeader)

    getRouteDetails(requestHeader) match {
      case Some(details) =>
        future.onComplete {
          time(timer) { statusCode =>
            httpRequestLatency.labelValues(details.method,
                                           details.route,
                                           statusCode)
          }
        }

      case None =>
        httpRequestMismatch.inc()
    }

    future
  }

  private def getRouteDetails(
      requestHeader: RequestHeader): Option[RouteDetails] = {
    for {
      method        <- requestHeader.tags.get("ROUTE_VERB")
      routePattern  <- requestHeader.tags.get("ROUTE_PATTERN")
      route         =  routePattern.replaceAll("<.*?>", "").replaceAll("\\$", ":")
    } yield RouteDetails(method, route)
  }

  private def statusCodeLabel(result: Result) =
    (result.header.status / 100) + "xx"

  private def time(timer: Timer)(
      templatedHistogram: String => LabelledHistogram): Try[Result] => Unit = {
    case Success(result) =>
      templatedHistogram(statusCodeLabel(result)).observeDuration(timer)
    case Failure(_) =>
      templatedHistogram(ServerErrorClass).observeDuration(timer)
  }

}
