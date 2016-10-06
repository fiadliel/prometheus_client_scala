package org.lyranthe.prometheus.client.scala.internal.counter

import org.lyranthe.prometheus.client.scala._

/** This represents a Prometheus counter with no labels.
  *
  * A Prometheus counter should be used for values which only increase in value.
  *
  * @param name The name of the counter
  */
final class Counter0(val name: String, val help: String) extends Collector {
  private[scala] val adder =
    new LabelledCounter(name, List.empty, new UnsynchronizedAdder)

  def incBy(v: Double): Unit =
    adder.incBy(v)

  def inc(): Unit =
    adder.inc

  override def collect(): List[RegistryMetric] =
    RegistryMetric(name, List.empty, adder.sum) :: Nil

  override def toString: String =
    s"Counter0($name)()"
}
