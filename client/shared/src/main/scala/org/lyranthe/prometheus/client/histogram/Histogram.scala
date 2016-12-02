package org.lyranthe.prometheus.client.histogram

import org.lyranthe.prometheus.client.internal.{
  UnsynchronizedDoubleAdder,
  UnsynchronizedLongAdder
}

private[client] object Histogram {
  def observe(adders: (UnsynchronizedDoubleAdder,
                       Array[(Double, UnsynchronizedLongAdder)]),
              v: Double): Unit = {
    var i: Int = 0
    while (i < adders._2.length) {
      val indexed = adders._2(i)
      if (v <= indexed._1)
        indexed._2.add(1)
      i = i + 1
    }

    adders._1.add(v)
  }
}
