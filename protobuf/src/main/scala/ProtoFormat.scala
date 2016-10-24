package org.lyranthe.prometheus.client.registry

import java.io.ByteArrayOutputStream

import io.prometheus.client.{Metrics => PB}
import org.lyranthe.prometheus.client._

object ProtoFormat extends RegistryFormat {
  override val contentType =
    "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited"

  def labelPairs(labels: List[(LabelName, String)]): List[PB.LabelPair] = {
    labels.map(lp =>
      PB.LabelPair.newBuilder.setName(lp._1.name).setValue(lp._2).build)
  }

  def convertBucket(bucket: Bucket): PB.Bucket = {
    PB.Bucket.newBuilder
      .setCumulativeCount(bucket.cumulativeCount)
      .setUpperBound(bucket.upperBound)
      .build
  }

  def convertMetric(metric: Metric): PB.Metric = {
    import scala.collection.JavaConverters._

    val newMetric = PB.Metric.newBuilder
    newMetric.addAllLabel(labelPairs(metric.labels).asJava)

    metric match {
      case GaugeMetric(labels, value) =>
        newMetric.setGauge(PB.Gauge.newBuilder.setValue(value))
      case CounterMetric(labels, value) =>
        newMetric.setCounter(PB.Counter.newBuilder.setValue(value))
      case HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
        newMetric.setHistogram(
          PB.Histogram.newBuilder
            .setSampleCount(sampleCount)
            .setSampleSum(sampleSum)
            .addAllBucket(buckets.map(convertBucket).toIterable.asJava))
    }

    newMetric.build
  }

  def convertMetricType(metricType: MetricType): PB.MetricType = {
    metricType match {
      case MetricType.Counter   => PB.MetricType.COUNTER
      case MetricType.Gauge     => PB.MetricType.GAUGE
      case MetricType.Histogram => PB.MetricType.HISTOGRAM
    }
  }

  override def output(values: => Iterator[RegistryMetrics]): Array[Byte] = {
    import scala.collection.JavaConverters._

    val outputStream = new ByteArrayOutputStream(4096)

    values foreach { metric =>
      val proto = PB.MetricFamily.newBuilder
        .setName(metric.name.name)
        .setHelp(metric.help)
        .setType(convertMetricType(metric.metricType))
        .addAllMetric(metric.metrics.map(convertMetric).asJava)
        .build

      proto.writeTo(outputStream)
    }

    outputStream.toByteArray
  }
}
