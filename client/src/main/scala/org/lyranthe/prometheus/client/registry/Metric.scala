package org.lyranthe.prometheus.client.registry

import org.lyranthe.prometheus.client.LabelName

sealed trait Metric {
  def labels: List[LabelPair]
}

case class Bucket(cumulativeCount: Long, upperBound: Double)
case class LabelPair(name: LabelName, value: String)

case class GaugeMetric(labels: List[LabelPair], value: Double)   extends Metric
case class CounterMetric(labels: List[LabelPair], value: Double) extends Metric
case class HistogramMetric(labels: List[LabelPair],
                           sampleCount: Long,
                           sampleSum: Double,
                           buckets: Array[Bucket])
    extends Metric
