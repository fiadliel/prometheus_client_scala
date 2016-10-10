package org.lyranthe.prometheus.client.internal.gauge

import org.lyranthe.prometheus.client._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
final class Gauge0(val name: String,
                   val help: String,
                   initialValue: Option[Double] = None)
    extends LabelledGauge(name, List.empty, new SynchronizedAdder)
    with Collector {
  def collectorType: String = "gauge"

  override def collect(): List[RegistryMetric] =
    synchronized {
      RegistryMetric(name, List.empty, adder.sum()) :: Nil
    }

  override def toString: String =
    s"Gauge0($name)()"
}
