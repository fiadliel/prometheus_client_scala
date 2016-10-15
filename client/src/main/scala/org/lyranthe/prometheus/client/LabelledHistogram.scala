package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.UnsynchronizedAdder

class LabelledHistogram(name: String,
                        labels: List[String],
                        val adder: Array[UnsynchronizedAdder],
                        buckets: List[(Double, Int)]) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, adder, v)
}
