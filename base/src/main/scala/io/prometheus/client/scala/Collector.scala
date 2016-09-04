package io.prometheus.client.scala

trait Collector[+N <: String] {
  def name: N
  def register(implicit registry: Registry): this.type = {
    registry.register(this)
    this
  }

  def collect(): List[RegistryMetric]
}
