package org.lyranthe.prometheus.client.play.filters

import org.lyranthe.prometheus.client.{play => _, _}
import org.lyranthe.prometheus.client.histogram.LabelledHistogram
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PrometheusFilter()(implicit val registry: Registry, executionContext: ExecutionContext) extends Filter {

  private val ClientErrorClass = "4xx"

  private val ServerErrorClass = "5xx"

  private val RouteRegex = "[/a-zA-Z0-9$_\\-]+".r

  private val httpHistogramBuckets = {
    val buckets = for(p <- Vector[Double](0.0001, 0.001, 0.01, 0.1, 1, 10); s <- Vector(1, 2, 5)) yield (p * s)
    HistogramBuckets(buckets: _*)
  }


  private val httpRequestLatency =
    Histogram(metric"http_request_duration_seconds", "Duration of HTTP request in seconds")(httpHistogramBuckets)
      .labels(label"path", label"status")
      .register


  private val counter = Counter(metric"http_request_route_not_found", "Route not found")
    .labels()
    .register

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val future = nextFilter(requestHeader)
    val routeOpt = for {
      routePattern <- requestHeader.tags.get("ROUTE_PATTERN")
      route <- RouteRegex findFirstIn routePattern
    } yield {
      future.onComplete(this.time(statusCode => httpRequestLatency.labelValues(route, statusCode)))
      route
    }
    if (routeOpt.isEmpty) {
      counter.inc()
    }
    future
  }

  private def statusCodeLabel(result: Result) = (result.header.status / 100) + "xx"

  private def time(templatedHistogram: String => LabelledHistogram)(implicit timer: Timer = Timer()): Try[Result] => Unit = {
    case Success(result) => templatedHistogram(statusCodeLabel(result)).observeDuration(timer)
    case Failure(_: IllegalArgumentException) => templatedHistogram(ClientErrorClass).observeDuration(timer)
    case Failure(_) => templatedHistogram(ServerErrorClass).observeDuration(timer)
  }

}