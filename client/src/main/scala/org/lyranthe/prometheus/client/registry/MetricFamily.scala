package org.lyranthe.prometheus.client.registry

import org.lyranthe.prometheus.client._

/** A metric family represents a counter, gauge or histogram metric.
  *
  * These
  */
trait MetricFamily {
  def name: MetricName
  def help: String
  def metricType: MetricType

  private[client] def escapedHelp: String

  def register(implicit registry: Registry): this.type = {
    registry.register(this)
    this
  }

  def registerAndReturnSuccess(implicit registry: Registry): Boolean =
    registry.register(this)

  def collect(): List[Metric]
}
