package org.lyranthe.prometheus.client.internal.histogram

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  */
private[client] final case class Histogram0(name: MetricName, help: String, buckets: List[(Double, Int)])
    extends LabelledHistogram(name, List.empty, Array.fill(buckets.size + 1)(new UnsynchronizedAdder), buckets)
    with PrefixedCollector {
  override final val collectorType = CollectorType.Histogram

  override def collect(): List[RegistryMetric] = {
    RegistryMetric(Some("total"), List.empty, adder.last.sum()) ::
      RegistryMetric(Some("sum"), List.empty, adder(adder.length - 2).sum()) ::
        buckets.map {
          case (bucket, idx) =>
            RegistryMetric(Some("bucket"),
                           List(label"le" -> HistogramBuckets.prometheusDoubleFormat(bucket)),
                           adder(idx).sum())
        }
  }
}
