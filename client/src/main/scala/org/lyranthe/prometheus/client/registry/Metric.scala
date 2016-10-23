package org.lyranthe.prometheus.client.registry

import org.lyranthe.prometheus.client.LabelName

sealed trait Metric {
  def labels: List[(LabelName, String)]
}

case class Bucket(cumulativeCount: Long, upperBound: Double)

case class GaugeMetric(labels: List[(LabelName, String)], value: Double) extends Metric
case class CounterMetric(labels: List[(LabelName, String)], value: Double) extends Metric
case class HistogramMetric(labels: List[(LabelName, String)], sampleCount: Long, sampleSum: Double, buckets: Array[Bucket]) extends Metric
