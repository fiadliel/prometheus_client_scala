package org.lyranthe.prometheus.client

import scala.util.matching.Regex

case class MetricName(name: String) extends AnyVal

object MetricName {
  val PrometheusMetricFormat: Regex = """^[a-zA-Z_:][a-zA-Z0-9_:]*$""".r

  def unsafeFromString(s: String): MetricName = {
    PrometheusMetricFormat.findFirstIn(s) match {
      case None =>
        throw new IllegalArgumentException(s"$s does not match required metric format ${PrometheusMetricFormat.regex}")
      case Some(formatted) =>
        MetricName(formatted)
    }
  }
}
