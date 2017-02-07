package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

trait Collector {
  def collect(): Iterator[RegistryMetrics]
}

trait Registry extends Collector {
  def register(collector: Collector): Boolean
  def register(c: MetricFamily): Boolean

  def output(format: RegistryFormat): Array[Byte] =
    format.output(collect)

  def outputText: String =
    new String(TextFormat.output(collect))
}
