package org.lyranthe.prometheus.client.internal

import org.lyranthe.prometheus.client.{CollectorType, Registry, RegistryMetric}

private[client] trait Collector {
  def underlyingName: Option[String]
  def help: String
  def collectorType: CollectorType

  def register(implicit registry: Registry): this.type = {
    registry.register(this)
    this
  }

  def collect(): List[RegistryMetric]
}
