package org.lyranthe.prometheus.client.scala

import org.lyranthe.prometheus.client.scala.internal.DefaultRegistry

trait Registry {
  def register(c: Collector): Unit
  def collect(): List[RegistryMetric]
}

object Registry {
  implicit val defaultRegistry: Registry = new DefaultRegistry
}
