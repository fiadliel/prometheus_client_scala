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

  def unsafeRegister(implicit registry: Registry): this.type = {
    registry.unsafeRegister(this)
    this
  }

  def collect(): List[Metric]
}
