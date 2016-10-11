package org.lyranthe.prometheus.client

sealed abstract class CollectorType extends Product with Serializable

object CollectorType {
  case object Counter extends CollectorType
  case object Gauge extends CollectorType
  case object Histogram extends CollectorType
}
