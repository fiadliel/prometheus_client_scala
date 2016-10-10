package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.{HistogramBuckets, UnsynchronizedAdder}

class LabelledHistogram(
    name: String,
    labels: List[String],
    val adder: Array[UnsynchronizedAdder])(implicit hb: HistogramBuckets) {
  val buckets = hb.buckets.zipWithIndex

  def observe(v: Double): Unit =
    Histogram.observe(buckets, adder, v)
}
