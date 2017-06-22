package org.lyranthe.prometheus.client.integration.akkahttp

import akka.http.scaladsl.server.{Directive, Directive0, Directives, Route}
import org.lyranthe.prometheus.client._

trait Metrics {

  /**
    * Returns the Akka Http Route to expose the metrics
    */
  val routes: Route

  /**
    * Records metrics for a given endpoint.
    */
  def withMetrics(endpoint: Option[String] = None): Directive0
}

trait PrometheusRoutes extends Directives {

  val metricsDirective: Directive[Unit]

  implicit val registry: Registry

  val routes: Route = metricsDirective {
    get {
      complete(registry.outputText)
    }
  }

}

class PrometheusMetrics(override val metricsDirective: Directive[Unit] = Directives.path("metrics"))(implicit val registry: Registry)
  extends Metrics with PrometheusRoutes {

  /**
    *
    * WARNING: every recorded endpoint stores a new time series in database. Do not record variable path parameters (eg: /brands/123,
    * /brands/456) or query params to avoid to dramatically increase the amount of data stored. When None is used as endpoint make
    * sure to position the directive at a level where the matched path cannot produce unbounded sets of values.
    *
    * @param endpoint if None uses [[akka.http.scaladsl.server.Directives.extractMatchedPath]]
    */
  def withMetrics(endpoint: Option[String] = None): Directive0 = extractRequestContext.flatMap { ctx =>
    val timer = Timer()
    extractMatchedPath.flatMap { path =>
      mapResponse { resp =>
        val label = endpoint.getOrElse(path.toString())
        val method = ctx.request.method.name
        val statusCode = s"${resp.status.intValue / 100}xx"

        recordHttpRequestDuration(method, label, statusCode, timer)

        resp
      }
    }
  }

  def recordHttpRequestDuration(method: String, endpoint: String, statusCode: String, timer: Timer): Unit =
    httRequestDuration.labelValues(method, endpoint, statusCode).observeDuration(timer)

  private val httpHistogramBuckets = {
    val buckets = for (
      p <- Vector[Double](0.0001, 0.001, 0.01, 0.1, 1, 10);
      s <- Vector(1, 2, 5)
    ) yield p * s
    HistogramBuckets(buckets: _*)
  }

  private val httRequestDuration = Histogram(metric"http_request_duration_seconds", "Duration of HTTP request in seconds")(httpHistogramBuckets)
    .labels(label"method", label"path", label"status")
    .register
}
