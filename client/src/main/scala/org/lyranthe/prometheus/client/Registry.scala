package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

trait Registry {
  def unsafeRegister(c: MetricFamily): Unit
  def collect(): Iterator[RegistryMetrics]

  def output(format: RegistryFormat): Iterator[Array[Byte]] =
    format.output(collect)

  def outputText: String =
    TextFormat.output(collect).map(new String(_)).mkString
}
