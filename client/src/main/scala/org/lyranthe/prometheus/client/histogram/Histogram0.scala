package org.lyranthe.prometheus.client.histogram

import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.internal._
import org.lyranthe.prometheus.client.registry._

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  */
final case class Histogram0 private[client] (name: MetricName,
                                             help: String,
                                             bucketValues: List[Double])
    extends LabelledHistogram(
      name,
      List.empty,
      (new UnsynchronizedDoubleAdder, bucketValues.view.sorted.map {
        _ -> new UnsynchronizedLongAdder
      }.to(Array)))
    with MetricFamily {
  override val metricType = MetricType.Histogram

  override final val escapedHelp =
    help.replace("\\", "\\\\").replace("\n", "\\n")

  override def collect(): List[HistogramMetric] = {
    List(
      HistogramMetric(List.empty,
                      buckets._2.last._2.sum,
                      buckets._1.sum,
                      buckets._2.map {
                        case (bucket, adder) => Bucket(adder.sum, bucket)
                      }))
  }
}
