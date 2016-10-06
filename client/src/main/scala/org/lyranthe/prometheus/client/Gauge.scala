package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.gauge._

object Gauge {
  def apply(name: String, help: String): UnlabelledGauge =
    UnlabelledGauge(name, help)
}
