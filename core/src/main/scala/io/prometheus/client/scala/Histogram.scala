package io.prometheus.client.scala

import io.prometheus.client.scala.internal.histogram._

object Histogram {
  def apply(name: String, help: String)(implicit hb: HistogramBuckets): UnlabelledHistogram = UnlabelledHistogram(name, help)
}
