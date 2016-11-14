package org.lyranthe.prometheus.client.histogram

import org.lyranthe.prometheus.client.{LabelName, MetricName, Timer}
import org.lyranthe.prometheus.client.internal.{
  NanoTimeSource,
  UnsynchronizedDoubleAdder,
  UnsynchronizedLongAdder
}

class LabelledHistogram private[client] (
    name: MetricName,
    labels: List[LabelName],
    val buckets: (UnsynchronizedDoubleAdder,
                  Array[(Double, UnsynchronizedLongAdder)])) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, v)

  def observeDuration(timer: Timer)(
      implicit timeSource: NanoTimeSource): Unit =
    Histogram.observe(buckets, timer.duration)
}
