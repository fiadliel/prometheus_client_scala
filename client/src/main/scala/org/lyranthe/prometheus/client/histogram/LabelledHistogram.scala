package org.lyranthe.prometheus.client.histogram

import org.lyranthe.prometheus.client.{LabelName, MetricName}
import org.lyranthe.prometheus.client.internal.{UnsynchronizedDoubleAdder, UnsynchronizedLongAdder}

class LabelledHistogram private[client] (
    name: MetricName,
    labels: List[LabelName],
    val buckets: (UnsynchronizedDoubleAdder, Array[(Double, UnsynchronizedLongAdder)])) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, v)
}
