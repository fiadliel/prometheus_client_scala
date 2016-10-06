package org.lyranthe.prometheus.client.internal.gauge

import org.lyranthe.prometheus.client.SynchronizedAdder

class LabelledGauge(name: String,
                    labels: List[String],
                    adder: SynchronizedAdder) {

  def incBy(v: Double): Unit = adder.add(v)

  def inc(): Unit = adder.add(1d)

  def decBy(v: Double): Unit = adder.add(-v)

  def dec(): Unit = adder.add(-1d)

  def set(v: Double): Unit = adder.set(v)

  def setToCurrentTime() = set(System.nanoTime() / 1e9)

  def sum(): Double =
    adder.sum()

  override def toString(): String =
    s"Counter($name)(${labels.mkString(",")})"
}
