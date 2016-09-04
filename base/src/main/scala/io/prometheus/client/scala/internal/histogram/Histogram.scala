package io.prometheus.client.scala.internal.histogram

import java.util.concurrent.atomic.DoubleAdder

import io.prometheus.client.scala._

object Histogram {
  def observe(bucketValues: Seq[(Double, Int)], buckets: Array[DoubleAdder], v: Double): Unit = {
    bucketValues.foreach {
      case (upperBound, idx) =>
        if (v <= upperBound)
          buckets(idx).add(1d)
    }

    // Last value in array contains sum of observations
    buckets(buckets.length - 1).add(v)
  }

  def bucketsWithInf(buckets: Seq[Double]): List[Double] = {
    val sortedBuckets = buckets.sorted

    val withInf =
      if (sortedBuckets.last == Double.MaxValue)
        sortedBuckets
      else
        sortedBuckets :+ Double.MaxValue

    withInf.toList
  }
}

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  * @tparam N The singleton type for the internal.histogram's name
  */
final class Histogram0[N <: String](val name: N, _buckets: Seq[Double])() extends Collector[N] {
  val buckets = Histogram.bucketsWithInf(_buckets).zipWithIndex

  private[scala] val adder = Array.fill(buckets.size + 1)(new DoubleAdder)

  def observe(v: Double): Unit = Histogram.observe(buckets, adder, v)

  override def collect(): List[RegistryMetric] = {
    RegistryMetric(s"${name}_total" , Vector.empty, adder.last.sum()) ::
      buckets.map { case (bucket, idx) =>
        RegistryMetric(s"${name}_$bucket", Vector.empty, adder(idx).sum())
      }
  }

  override def toString(): String =
    s"Histogram0($name, ${buckets.map(_._1)})()"
}

/** This represents a Prometheus internal.histogram with 1 label.
  **
  * @param name The name of the internal.histogram
  * @tparam N The singleton type for the internal.histogram's name
  * @tparam L1 The singleton string type for label 1
  */
final class Histogram1[N <: String, L1 <: String](val name: N, _buckets: Seq[Double])(label: String) extends Collector[N] {
  val buckets = Histogram.bucketsWithInf(_buckets).zipWithIndex

  private[scala] val adders = new BucketedAdders[String](buckets.size + 1, None)

  def observe(l1: String)(v: Double): Unit = Histogram.observe(buckets, adders(l1), v)

  def collect(): List[RegistryMetric] =
    adders.getAll.flatMap({
      case (labelValue, value) =>
        RegistryMetric(s"${name}_total" , Vector.empty, value.last) ::
        buckets.map { case (bucket, idx) =>
          RegistryMetric(s"${name}_$bucket", Vector.empty, value(idx))
        }
    })

  override def toString(): String =
    s"Histogram1($name, ${buckets.map(_._1)})($label)"
}
