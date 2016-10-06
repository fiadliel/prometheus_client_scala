package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.UnsynchronizedAdder

class LabelledHistogram(name: String,
                        labels: List[String],
                        buckets: Seq[(Double, Int)],
                        adder: Array[UnsynchronizedAdder]) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, adder, v)
}
