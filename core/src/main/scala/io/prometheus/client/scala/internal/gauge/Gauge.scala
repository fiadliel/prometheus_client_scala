package io.prometheus.client.scala.internal.gauge

import java.util.concurrent.atomic.DoubleAdder

import io.prometheus.client.scala._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
final class Gauge0(val name: String, initialValue: Option[Double] = None)
    extends Collector {
  private[scala] val adder = new DoubleAdder
  initialValue.foreach(adder.add)

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

  override def collect(): List[RegistryMetric] =
    synchronized {
      RegistryMetric(name, List.empty, adder.sum()) :: Nil
    }

  override def toString: String =
    s"Gauge0($name)()"
}
