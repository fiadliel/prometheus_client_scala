package org.lyranthe.prometheus.client.counter

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._
import org.lyranthe.prometheus.client.registry._

/** This represents a Prometheus counter metric with 0 labels.
  *
  * A counter contains a value that can only be incremented.
  *
  * @param name The name of the counter.
  * @param help The help text for the counter.
  */
final case class Counter0 private[client] (name: MetricName, help: String)
    extends LabelledCounter(name, List.empty, new UnsynchronizedDoubleAdder)
    with MetricFamily {
  override val metricType = MetricType.Counter

  override final val escapedHelp =
    help.replace("\\", "\\\\").replace("\n", "\\n")

  override def collect(): List[Metric] =
    CounterMetric(List.empty, adder.sum) :: Nil
}
