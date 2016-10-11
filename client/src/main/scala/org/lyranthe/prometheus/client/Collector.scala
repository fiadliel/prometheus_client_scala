package org.lyranthe.prometheus.client

trait Collector {
  def name: String
  def help: String
  def collectorType: CollectorType

  def register(implicit registry: Registry): this.type = {
    registry.register(this)
    this
  }

  def collect(): List[RegistryMetric]
}
