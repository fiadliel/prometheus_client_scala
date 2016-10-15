package org.lyranthe.prometheus.client

sealed abstract class CollectorType extends Product with Serializable

object CollectorType {
  case object Counter extends CollectorType {
    override val toString: String = "counter"
  }

  case object Gauge extends CollectorType {
    override val toString: String = "gauge"
  }

  case object Histogram extends CollectorType {
    override val toString: String = "histogram"
  }
}
