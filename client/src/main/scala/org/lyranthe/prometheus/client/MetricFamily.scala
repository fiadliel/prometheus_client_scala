package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Metric

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
