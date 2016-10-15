package org.lyranthe.prometheus.client.internal.gauge

import org.lyranthe.prometheus.client._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
final case class Gauge0(name: String, help: String, initialValue: Option[Double] = None)
    extends LabelledGauge(name, List.empty, new SynchronizedAdder)
    with Collector {
  override final val collectorType = CollectorType.Gauge

  override def collect(): List[RegistryMetric] =
    synchronized {
      RegistryMetric(name, List.empty, adder.sum()) :: Nil
    }
}
