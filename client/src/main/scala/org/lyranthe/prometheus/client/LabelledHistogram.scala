package org.lyranthe.prometheus.client

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
    histogram.Histogram.observe(buckets, v)

  def observeDuration(timer: Timer)(
      implicit timeSource: NanoTimeSource): Unit =
    histogram.Histogram.observe(buckets, timer.duration)
}
