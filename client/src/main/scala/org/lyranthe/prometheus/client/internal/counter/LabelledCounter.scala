package org.lyranthe.prometheus.client.internal.counter

import org.lyranthe.prometheus.client.internal.{LabelName, MetricName, UnsynchronizedAdder}

private[client] class LabelledCounter(name: MetricName, labels: List[LabelName], val adder: UnsynchronizedAdder) {
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
