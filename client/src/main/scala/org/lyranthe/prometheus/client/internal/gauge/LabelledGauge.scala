package org.lyranthe.prometheus.client.internal.gauge

import java.time.{Clock, Duration, Instant}

import org.lyranthe.prometheus.client.{LabelName, MetricName}
import org.lyranthe.prometheus.client.internal.SynchronizedDoubleAdder

class LabelledGauge private[client] (name: MetricName,
                                     labels: List[LabelName],
                                     val adder: SynchronizedDoubleAdder,
                                     initialValue: Option[Double] = None) {
  initialValue foreach adder.add

  def incBy(v: Double): Unit = adder.add(v)

  def inc(): Unit = adder.add(1d)

  def decBy(v: Double): Unit = adder.add(-v)

  def dec(): Unit = adder.add(-1d)

  def set(v: Double): Unit = adder.set(v)

  def setToCurrentTime()(implicit clock: Clock): Unit =
    set(Instant.now(clock).getEpochSecond)

  def setToDuration(duration: Duration): Unit =
    set((duration.getSeconds * 1e9 + duration.getNano) / 1e9)

  def setToInstant(instant: Instant): Unit =
    set(instant.getEpochSecond)

  def sum(): Double =
    adder.sum()

  override def toString(): String =
    s"Counter($name)(${labels.mkString(",")})"
}
