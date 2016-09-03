package io.prometheus.client.scala.counter

import java.util.concurrent.atomic.DoubleAdder

import io.prometheus.client.scala._


/** This represents a Prometheus internal.counter with no labels.
  *
  * A Prometheus internal.counter should be used for values which only increase in value.
  *
  * @param name The name of the internal.counter
  * @tparam N The singleton type for the internal.counter's name
  */
final class Counter0[N <: String](val name: N) extends Collector[N] {
  private[scala] val adder = new DoubleAdder

  def incBy(v: Double): Unit = adder.add(v)

  def inc(): Unit = adder.add(1d)
}

/** This represents a Prometheus internal.counter with 1 label.
  *
  * A Prometheus internal.counter should be used for values which only increase in value.
  *
  * @param name The name of the internal.counter
  * @tparam N The singleton type for the internal.counter's name
  * @tparam L1 The singleton string type for label 1
  */
final class Counter1[N <: String, L1 <: String](val name: N) extends Collector[N] {
  private[scala] val adders = new Adders[String]

  def incBy(l1: String)(v: Double): Unit = {
    adders(l1).add(v)
  }

  def inc(l1: String): Unit =
    adders(l1).add(1d)
}
