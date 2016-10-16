package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.MetricName
import org.lyranthe.prometheus.client.internal.gauge._

object Gauge {
  def apply(name: MetricName, help: String): UnlabelledGauge =
    UnlabelledGauge(name.name, help)
}
