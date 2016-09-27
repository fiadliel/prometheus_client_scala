package io.prometheus.client.scala

import io.prometheus.client.scala.internal.gauge._

object Gauge {
  def apply(name: String, help: String): UnlabelledGauge = UnlabelledGauge(name, help)
}
