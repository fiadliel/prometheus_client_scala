package org.lyranthe.prometheus.client.gauge

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._
import org.lyranthe.prometheus.client.registry._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
final case class Gauge0 private[client] (name: MetricName,
                                         help: String,
                                         initialValue: Option[Double] = None)
    extends LabelledGauge(name, List.empty, new SynchronizedDoubleAdder)
    with MetricFamily {
  override val metricType = MetricType.Gauge

  override final val escapedHelp =
    help.replace("\\", "\\\\").replace("\n", "\\n")

  override def collect(): List[Metric] =
    synchronized {
      GaugeMetric(List.empty, adder.sum) :: Nil
    }
}
