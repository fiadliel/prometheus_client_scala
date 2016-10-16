package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.{LabelName, MetricName}
import org.lyranthe.prometheus.client.internal.UnsynchronizedAdder

private[client] class LabelledHistogram(name: MetricName,
                                        labels: List[LabelName],
                                        val adder: Array[UnsynchronizedAdder],
                                        buckets: List[(Double, Int)]) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, adder, v)
}
