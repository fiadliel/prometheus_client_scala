package org.lyranthe.prometheus.client.scala

trait HistogramBuckets {
  def buckets: List[Double]
}

object HistogramBuckets {
  def bucketsWithInf(buckets: Seq[Double]): List[Double] = {
    val sortedBuckets = buckets.sorted

    val withInf =
      if (sortedBuckets.last == Double.PositiveInfinity)
        sortedBuckets
      else
        sortedBuckets :+ Double.PositiveInfinity

    withInf.toList
  }

  def prometheusDoubleFormat(d: Double) = {
    if (d == Double.PositiveInfinity)
      "+Inf"
    else if (d == Double.NegativeInfinity)
      "-Inf"
    else if (d.isNaN)
      "NaN"
    else
      d.toString
  }

  def apply(bucketList: Double*) = new HistogramBuckets {
    override val buckets = bucketsWithInf(bucketList)
  }
}
