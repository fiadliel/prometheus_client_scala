package org.lyranthe.prometheus.client.scala

import org.lyranthe.prometheus.client.scala.internal.gauge._

object Gauge {
  def apply(name: String, help: String): UnlabelledGauge = UnlabelledGauge(name, help)
}
