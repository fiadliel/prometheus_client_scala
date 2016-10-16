package org.lyranthe.prometheus.client.internal.gauge

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
private[client] final case class Gauge0(name: String, help: String, initialValue: Option[Double] = None)
    extends LabelledGauge(name, List.empty, new SynchronizedAdder)
    with PrefixedCollector {
  override final val collectorType = CollectorType.Gauge

  override def collect(): List[RegistryMetric] =
    synchronized {
      RegistryMetric(None, List.empty, adder.sum()) :: Nil
    }
}
