package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.histogram.UnlabelledHistogram

import scala.language.experimental.macros

object Histogram {
  def apply(metric: MetricName, help: String)(implicit hb: HistogramBuckets): UnlabelledHistogram = {
    UnlabelledHistogram(metric, help, hb)
  }
}
