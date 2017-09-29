package org.lyranthe.prometheus.client

import java.time.Duration
import org.lyranthe.prometheus.client.internal.NanoTimeSource

case class Timer(startTimeNanos: Long) extends AnyVal {
  def seconds(implicit timeSource: NanoTimeSource): Double =
    (System.nanoTime - startTimeNanos) / 1e9

  def duration(implicit timeSource: NanoTimeSource): Duration =
    Duration.ofNanos(System.nanoTime - startTimeNanos)
}

object Timer {
  def apply()(implicit timeSource: NanoTimeSource): Timer =
    Timer(timeSource.nanoTime)
}
