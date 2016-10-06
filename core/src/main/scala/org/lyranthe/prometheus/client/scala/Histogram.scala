package org.lyranthe.prometheus.client.scala

import org.lyranthe.prometheus.client.scala.internal.histogram._

object Histogram {
  def apply(name: String, help: String)(
      implicit hb: HistogramBuckets): UnlabelledHistogram =
    UnlabelledHistogram(name, help)
}
