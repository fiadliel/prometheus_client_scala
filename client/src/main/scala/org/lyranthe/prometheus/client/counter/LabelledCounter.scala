package org.lyranthe.prometheus.client.counter

import org.lyranthe.prometheus.client.{LabelName, MetricName}
import org.lyranthe.prometheus.client.internal.UnsynchronizedDoubleAdder

/** Represents counter and associated labels.
  *
  * @param name The name of this counter.
  * @param labels The labels attached to this counter.
  * @param adder The adder responsible for storing the underlying value.
  */
class LabelledCounter private[client] (name: MetricName,
                                       labels: List[LabelName],
                                       val adder: UnsynchronizedDoubleAdder) {
  def incBy(v: Double): Unit = {
    assert(v >= 0d)
    adder.add(v)
  }

  def inc(): Unit =
    adder.add(1d)

  def sum(): Double =
    adder.sum()

  override def toString(): String =
    s"Counter($name)(${labels.mkString(",")})"
}
