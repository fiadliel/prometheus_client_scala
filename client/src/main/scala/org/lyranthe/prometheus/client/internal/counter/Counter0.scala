package org.lyranthe.prometheus.client.internal.counter

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._

/** This represents a Prometheus counter with no labels.
  *
  * A Prometheus counter should be used for values which only increase in value.
  *
  * @param name The name of the counter
  */
private[client] final case class Counter0(name: MetricName, help: String)
    extends LabelledCounter(name, List.empty, new UnsynchronizedDoubleAdder)
    with MetricFamily {
  override val metricType = MetricType.Counter

  override def collect(): List[Metric] =
    CounterMetric(List.empty, adder.sum) :: Nil
}
