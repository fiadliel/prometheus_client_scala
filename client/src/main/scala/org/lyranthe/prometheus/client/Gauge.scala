package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.gauge.UnlabelledGauge

object Gauge {
  def apply(name: MetricName, help: String): UnlabelledGauge =
    UnlabelledGauge(name, help)
}
