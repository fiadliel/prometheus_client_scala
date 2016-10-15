package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client._

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  */
final case class Histogram0(name: String, help: String)(implicit hb: HistogramBuckets)
  extends LabelledHistogram(name, List.empty, Array.fill(hb.buckets.size + 1)(new UnsynchronizedAdder))
    with Collector {
  override final val collectorType = CollectorType.Histogram

  override def collect(): List[RegistryMetric] = {
    RegistryMetric(s"${name}_total", List.empty, adder.last.sum()) ::
      RegistryMetric(s"${name}_sum", List.empty, adder(adder.length - 2).sum()) ::
      buckets.map {
        case (bucket, idx) =>
          RegistryMetric(s"${name}_bucket",
            List("le" -> HistogramBuckets.prometheusDoubleFormat(bucket)),
            adder(idx).sum())
      }
  }
}
