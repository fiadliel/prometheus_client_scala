package io.prometheus.client.scala.gauge

import java.util.concurrent.atomic.DoubleAdder
import java.util.concurrent.locks.ReentrantReadWriteLock

import io.prometheus.client.scala._

//  def inc(): Unit
//  def inc(v: Double): Unit
//  def dec(): Unit
//  def dec(v: Double): Unit
//  def set(v: Double): Unit
//
//  def setToCurrentTime() = set(System.nanoTime() / 1e9)

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  * @tparam N The singleton type for the internal.gauge's name
  */
final class Gauge0[N <: String](val name: N) extends Collector[N] {
  private[scala] val adder = new DoubleAdder
  private[scala] val rwlock = new ReentrantReadWriteLock

  def incBy(v: Double): Unit = adder.add(v)

  def inc(): Unit = adder.add(1d)

  def decBy(v: Double): Unit = adder.add(-v)

  def dec(): Unit = adder.add(-1d)

  def set(v: Double): Unit =
    synchronized {
      adder.reset()
      adder.add(v)
    }

  def setToCurrentTime() = set(System.nanoTime() / 1e9)
}

/** This represents a Prometheus internal.gauge with 1 label.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  * @tparam N The singleton type for the internal.gauge's name
  * @tparam L1 The singleton string type for label 1
  */
final class Gauge1[N <: String, L1 <: String](val name: N) extends Collector[N] {
  private[scala] val adders = new Adders[String]

  def incBy(l1: String)(v: Double): Unit = {
    adders(l1).add(v)
  }

  def inc(l1: String): Unit =
    adders(l1).add(1d)

  def decBy(l1: String)(v: Double): Unit = adders(l1).add(-v)

  def dec(l1: String): Unit = adders(l1).add(-1d)

  def set(l1: String)(v: Double): Unit = {
    val adder = adders(l1)
    synchronized {
      adder.reset()
      adder.add(v)
    }
  }

  def setToCurrentTime(l1: String) = set(l1)(System.nanoTime() / 1e9)
}
