package io.prometheus.client.scala.internal.gauge

import java.util.concurrent.atomic.DoubleAdder

import io.prometheus.client.scala._

/** This represents a Prometheus internal.gauge with no labels.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  * @tparam N The singleton type for the internal.gauge's name
  */
final class Gauge0[N <: String](val name: N, initialValue: Option[Double] = None)() extends Collector[N] {
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

/** This represents a Prometheus internal.gauge with 1 label.
  *
  * A Prometheus internal.gauge should be used for values which go up and down.
  *
  * @param name The name of the internal.gauge
  * @tparam N The singleton type for the internal.gauge's name
  * @tparam L1 The singleton string type for label 1
  */
final class Gauge1[N <: String, L1 <: String](val name: N, initialValue: Option[Double] = None)(val label: String) extends Collector[N] {
  private[scala] val adders = new Adders[String](initialValue)

  def incBy(l1: String)(v: Double): Unit =
    adders(l1).add(v)

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

  override def collect(): List[RegistryMetric] =
    synchronized {
      adders.getAll.map({
        case (labelValue, value) => RegistryMetric(name, List(label -> labelValue), value)}
      )
    }

  override def toString: String =
    s"Gauge1($name)($label)"
}
