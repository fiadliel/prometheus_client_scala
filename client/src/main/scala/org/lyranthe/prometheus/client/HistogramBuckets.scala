package org.lyranthe.prometheus.client

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


  def apply(bucketList: Double*) = new HistogramBuckets {
    override val buckets: List[Double] = bucketsWithInf(bucketList)
    override def toString: String      = buckets.mkString("HistogramBuckets(", ",", ")")
  }
}
