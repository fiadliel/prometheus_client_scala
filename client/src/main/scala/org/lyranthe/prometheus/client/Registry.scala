package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

trait Registry {
  def unsafeRegister(c: MetricFamily): Unit
  def collect(): Iterator[RegistryMetrics]

  def output(format: RegistryFormat): Array[Byte] =
    format.output(collect)

  def outputText: String =
    new String(TextFormat.output(collect))
}
