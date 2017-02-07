package org.lyranthe.prometheus.client.registry

import java.io.ByteArrayOutputStream

import io.prometheus.client.{Metrics => PB}
import org.lyranthe.prometheus.client._

object ProtoFormat extends RegistryFormat {
  override val contentType =
    "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited"

  def labelPairs(labels: List[LabelPair]): List[PB.LabelPair] = {
    labels.map(lp =>
      PB.LabelPair.newBuilder.setName(lp.name.name).setValue(lp.value).build)
  }

  def convertBucket(bucket: Bucket): PB.Bucket = {
    PB.Bucket.newBuilder
      .setCumulativeCount(bucket.cumulativeCount)
      .setUpperBound(bucket.upperBound)
      .build
  }

  def convertQuantile(quantile: Quantile): PB.Quantile = {
    PB.Quantile.newBuilder
      .setValue(quantile.value)
      .setQuantile(quantile.quantile)
      .build
  }

  def convertMetric(metric: Metric): PB.Metric = {
    import scala.collection.JavaConverters._

    val newMetric = PB.Metric.newBuilder
    newMetric.addAllLabel(labelPairs(metric.labels).asJava)

    metric match {
      case CounterMetric(labels, value) =>
        newMetric.setCounter(PB.Counter.newBuilder.setValue(value))

      case GaugeMetric(labels, value) =>
        newMetric.setGauge(PB.Gauge.newBuilder.setValue(value))

      case HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
        newMetric.setHistogram(
          PB.Histogram.newBuilder
            .setSampleCount(sampleCount)
            .setSampleSum(sampleSum)
            .addAllBucket(buckets.map(convertBucket).toIterable.asJava))

      case SummaryMetric(labels, sampleCount, sampleSum, quantiles) =>
        newMetric.setSummary(
          PB.Summary.newBuilder
            .setSampleCount(sampleCount)
            .setSampleSum(sampleSum)
            .addAllQuantile(quantiles.map(convertQuantile).toIterable.asJava)
        )

      case UntypedMetric(labels, value) =>
        newMetric.setUntyped(PB.Untyped.newBuilder.setValue(value))
    }

    newMetric.build
  }

  def convertMetricType(metricType: MetricType): PB.MetricType = {
    metricType match {
      case MetricType.Counter   => PB.MetricType.COUNTER
      case MetricType.Gauge     => PB.MetricType.GAUGE
      case MetricType.Histogram => PB.MetricType.HISTOGRAM
      case MetricType.Summary   => PB.MetricType.SUMMARY
      case MetricType.Untyped   => PB.MetricType.UNTYPED
    }
  }

  override def output(values: => Iterator[RegistryMetrics]): Array[Byte] = {
    import scala.collection.JavaConverters._

    val outputStream = new ByteArrayOutputStream(1024)

    values foreach { metric =>
      val proto = PB.MetricFamily.newBuilder
        .setName(metric.name.name)
        .setHelp(metric.help)
        .setType(convertMetricType(metric.metricType))
        .addAllMetric(metric.metrics.map(convertMetric).asJava)
        .build

      proto.writeDelimitedTo(outputStream)
    }

    outputStream.toByteArray
  }
}
