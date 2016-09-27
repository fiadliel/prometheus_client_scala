package org.lyranthe.prometheus.client.scala.internal.counter

import org.lyranthe.prometheus.client.scala.UnsynchronizedAdder

class LabelledCounter(name: String, labels: List[String], adder: UnsynchronizedAdder) {

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
