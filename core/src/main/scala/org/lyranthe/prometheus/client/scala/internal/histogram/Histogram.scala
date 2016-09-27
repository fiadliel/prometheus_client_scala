package org.lyranthe.prometheus.client.scala.internal.histogram

import org.lyranthe.prometheus.client.scala._

object Histogram {
  def observe(bucketValues: Seq[(Double, Int)],
              buckets: Array[UnsynchronizedAdder],
              v: Double): Unit = {
    bucketValues.foreach {
      case (upperBound, idx) =>
        if (v <= upperBound)
          buckets(idx).add(1d)
    }

    // Last value in array contains sum of observations
    buckets(buckets.length - 1).add(v)
  }
}

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  */
final class Histogram0(val name: String, val help: String)(implicit hb: HistogramBuckets)
    extends Collector {
  val buckets = hb.buckets.zipWithIndex

  private[scala] val adder =
    Array.fill(buckets.size + 1)(new UnsynchronizedAdder)

  def observe(v: Double): Unit = Histogram.observe(buckets, adder, v)

  override def collect(): List[RegistryMetric] = {
    RegistryMetric(s"${name}_total", List.empty, adder.last.sum()) ::
      RegistryMetric(s"${name}_sum", List.empty, adder(adder.length - 2).sum()) ::
        buckets.map {
          case (bucket, idx) =>
            RegistryMetric(
              s"${name}_bucket",
              List("le" -> HistogramBuckets.prometheusDoubleFormat(bucket)),
              adder(idx).sum())
        }
  }

  override def toString: String =
    s"Histogram0($name, ${buckets.map(_._1)})()"
}
