package org.lyranthe.prometheus.client.scala.internal.histogram

import org.lyranthe.prometheus.client.scala.UnsynchronizedAdder

class LabelledHistogram(name: String,
                        labels: List[String],
                        buckets: Seq[(Double, Int)],
                        adder: Array[UnsynchronizedAdder]) {
  def observe(v: Double): Unit =
    Histogram.observe(buckets, adder, v)
}
