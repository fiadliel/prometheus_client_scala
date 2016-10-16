package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.histogram._

object Histogram {
  def apply(name: String, help: String)(implicit hb: HistogramBuckets): UnlabelledHistogram =
    UnlabelledHistogram(name, help)
}
