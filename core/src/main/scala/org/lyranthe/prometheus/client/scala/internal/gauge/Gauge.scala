package org.lyranthe.prometheus.client.scala.internal.gauge

import org.lyranthe.prometheus.client.scala._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  */
final class Gauge0(val name: String, val help: String, initialValue: Option[Double] = None) extends Collector {
  private[scala] val adder = new LabelledGauge(name, List.empty, new SynchronizedAdder())
  initialValue foreach adder.incBy

  def incBy(v: Double): Unit = adder.incBy(v)

  def inc(): Unit = adder.inc()

  def decBy(v: Double): Unit = adder.decBy(v)

  def dec(): Unit = adder.dec()

  def set(v: Double): Unit = adder.set(v)

  def setToCurrentTime(): Unit = set(System.nanoTime() / 1e9)

  override def collect(): List[RegistryMetric] =
    synchronized {
      RegistryMetric(name, List.empty, adder.sum()) :: Nil
    }

  override def toString: String =
    s"Gauge0($name)()"
}
