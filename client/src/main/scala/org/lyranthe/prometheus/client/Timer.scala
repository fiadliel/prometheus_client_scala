package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.NanoTimeSource

case class Timer(startTimeNanos: Long) extends AnyVal {
  def duration(implicit timeSource: NanoTimeSource): Double =
    (System.nanoTime - startTimeNanos) / 1e9
}

object Timer {
  def apply()(implicit timeSource: NanoTimeSource): Timer =
    Timer(timeSource.nanoTime)
}
