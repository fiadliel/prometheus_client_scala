package io.prometheus.client.scala.internal.histogram

import java.util.concurrent.atomic.DoubleAdder

import io.prometheus.client.scala._

object Histogram {
  def observe(bucketValues: IndexedSeq[(Double, Int)], buckets: Array[DoubleAdder], v: Double): Unit = {
    bucketValues.foreach {
      case (upperBound, idx) =>
        if (v <= upperBound)
          buckets(idx).add(1d)
    }

    // Last value in array contains sum of observations
    buckets(buckets.length - 1).add(v)
  }

  def bucketsWithInf(buckets: IndexedSeq[Double]): IndexedSeq[Double] = {
    val sortedBuckets = buckets.sorted

    if (sortedBuckets.last == Double.MaxValue)
      sortedBuckets
    else
      sortedBuckets :+ Double.MaxValue
  }
}

/** This represents a Prometheus internal.histogram with no labels.
  *
  * @param name The name of the internal.histogram
  * @tparam N The singleton type for the internal.histogram's name
  */
final class Histogram0[N <: String](val name: N, _buckets: IndexedSeq[Double]) extends Collector[N] {
  val buckets = Histogram.bucketsWithInf(_buckets).zipWithIndex

  private[scala] val adder = Array.fill(buckets.size + 1)(new DoubleAdder)

  def observe(v: Double): Unit = Histogram.observe(buckets, adder, v)
}

/** This represents a Prometheus internal.histogram with 1 label.
  **
  * @param name The name of the internal.histogram
  * @tparam N The singleton type for the internal.histogram's name
  * @tparam L1 The singleton string type for label 1
  */
final class Histogram1[N <: String, L1 <: String](val name: N, _buckets: IndexedSeq[Double]) extends Collector[N] {
  val buckets = Histogram.bucketsWithInf(_buckets).zipWithIndex

  private[scala] val adders = new BucketedAdders[String](buckets.size + 1)

  def observe(l1: String)(v: Double): Unit = Histogram.observe(buckets, adders(l1), v)
}
