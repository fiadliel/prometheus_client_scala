package org.lyranthe.prometheus.client.internal.gauge

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._
import org.lyranthe.prometheus.client.registry._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
private[client] final case class Gauge0(name: MetricName, help: String, initialValue: Option[Double] = None)
    extends LabelledGauge(name, List.empty, new SynchronizedDoubleAdder)
    with MetricFamily {
  override val metricType = MetricType.Gauge

  override def collect(): List[Metric] =
    synchronized {
      GaugeMetric(List.empty, adder.sum) :: Nil
    }
}
