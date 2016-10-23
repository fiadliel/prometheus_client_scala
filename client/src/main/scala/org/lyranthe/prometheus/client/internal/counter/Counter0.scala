package org.lyranthe.prometheus.client.internal.counter

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._

/** This represents a Prometheus counter metric with 0 labels.
  *
  * A counter contains a value that can only be incremented.
  *
  * @param name The name of the counter.
  * @param help The help text for the counter.
  */
private[client] final case class Counter0(name: MetricName, help: String)
    extends LabelledCounter(name, List.empty, new UnsynchronizedDoubleAdder)
    with MetricFamily {
  override val metricType = MetricType.Counter

  override def collect(): List[Metric] =
    CounterMetric(List.empty, adder.sum) :: Nil
}
