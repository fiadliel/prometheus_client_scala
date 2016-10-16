package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.internal.UnsynchronizedAdder

private[client] object Histogram {
  def observe(bucketValues: Seq[(Double, Int)], buckets: Array[UnsynchronizedAdder], v: Double): Unit = {
    bucketValues.foreach {
      case (upperBound, idx) =>
        if (v <= upperBound)
          buckets(idx).add(1d)
    }

    // Last value in array contains sum of observations
    buckets(buckets.length - 1).add(v)
  }
}
