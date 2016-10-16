package org.lyranthe.prometheus.client.internal.counter

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._

/** This represents a Prometheus counter with no labels.
  *
  * A Prometheus counter should be used for values which only increase in value.
  *
  * @param name The name of the counter
  */
private[client] final case class Counter0(name: String, help: String)
    extends LabelledCounter(name, List.empty, new UnsynchronizedAdder())
    with PrefixedCollector {
  override final val collectorType = CollectorType.Counter

  override def collect(): List[RegistryMetric] =
    RegistryMetric(None, List.empty, adder.sum) :: Nil
}
