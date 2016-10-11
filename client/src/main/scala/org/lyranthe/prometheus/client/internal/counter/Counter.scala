package org.lyranthe.prometheus.client.internal.counter

import org.lyranthe.prometheus.client._

/** This represents a Prometheus counter with no labels.
  *
  * A Prometheus counter should be used for values which only increase in value.
  *
  * @param name The name of the counter
  */
final class Counter0(val name: String, val help: String)
    extends LabelledCounter(name, List.empty, new UnsynchronizedAdder())
    with Collector {
  override final val collectorType = CollectorType.Counter

  override def collect(): List[RegistryMetric] =
    RegistryMetric(name, List.empty, adder.sum) :: Nil

  override def toString: String =
    s"Counter0($name)()"
}
