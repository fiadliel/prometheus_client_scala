package org.lyranthe.prometheus.client.internal

import org.lyranthe.prometheus.client.{CollectorType, MetricName, Registry, RegistryMetric}

private[client] trait Collector {
  def underlyingName: Option[MetricName]
  def help: String
  def collectorType: CollectorType

  def unsafeRegister(implicit registry: Registry): this.type = {
    registry.unsafeRegister(this)
    this
  }

  def collect(): List[RegistryMetric]
}
