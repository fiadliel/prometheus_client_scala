package org.lyranthe.prometheus.client

sealed abstract class MetricType extends Product with Serializable

object MetricType {
  case object Counter extends MetricType {
    override val toString: String = "counter"
  }

  case object Gauge extends MetricType {
    override val toString: String = "gauge"
  }

  case object Histogram extends MetricType {
    override val toString: String = "histogram"
  }
}
