package io.prometheus.client.scala

import io.prometheus.client.scala.internal.DefaultRegistry

trait Registry {
  def register(c: Collector[String]): Unit
  def collect(): List[RegistryMetric]
}

object Registry {
  implicit val defaultRegistry: Registry = new DefaultRegistry
}
