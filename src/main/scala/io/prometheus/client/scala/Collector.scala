package io.prometheus.client.scala

trait Collector[+N <: String] {
  def name: N
  def register(implicit registry: Registry): Unit =
    registry.register(this)

  def collect(): List[RegistryMetric]
}
