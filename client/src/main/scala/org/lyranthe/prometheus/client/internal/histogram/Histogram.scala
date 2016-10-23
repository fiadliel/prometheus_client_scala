package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client.internal.{UnsynchronizedDoubleAdder, UnsynchronizedLongAdder}

private[client] object Histogram {
  def observe(adders: (UnsynchronizedDoubleAdder, Array[(Double, UnsynchronizedLongAdder)]), v: Double): Unit = {
    adders._2.foreach {
      case (upperBound, adder) =>
        if (v <= upperBound)
          adder.add(1)
    }

    adders._1.add(v)
  }
}
