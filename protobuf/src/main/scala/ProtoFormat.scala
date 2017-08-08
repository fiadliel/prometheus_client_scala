package org.lyranthe.prometheus.client.registry

import java.io.ByteArrayOutputStream

import io.prometheus.client.{metrics => PB}
import org.lyranthe.prometheus.client._

object ProtoFormat extends RegistryFormat {
  override val contentType =
    "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited"

  def labelPairs(labels: List[LabelPair]): List[PB.LabelPair] = {
    labels.map(lp =>
      PB.LabelPair(name = Some(lp.name.name), value = Some(lp.value)))
  }

  def convertBucket(bucket: Bucket): PB.Bucket = {
    PB.Bucket(cumulativeCount = Some(bucket.cumulativeCount),
              upperBound = Some(bucket.upperBound))
  }

  def convertMetric(metric: Metric): PB.Metric = {
    val newMetric = PB.Metric(labelPairs(metric.labels))

    metric match {
      case GaugeMetric(labels, value) =>
        newMetric.copy(gauge = Some(PB.Gauge(value = Some(value))))
      case CounterMetric(labels, value) =>
        newMetric.copy(counter = Some(PB.Counter(value = Some(value))))
      case HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
        newMetric.copy(
          histogram = Some(
            PB.Histogram(sampleCount = Some(sampleCount),
                         sampleSum = Some(sampleSum),
                         bucket = buckets.map(convertBucket))))
    }
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

    val outputStream = new ByteArrayOutputStream(1024)

    values foreach { metric =>
      val proto = PB
        .MetricFamily(name = Some(metric.name.name),
                      help = Some(metric.help),
                      `type` = Some(convertMetricType(metric.metricType)),
                      metric = metric.metrics.map(convertMetric))

      proto.writeDelimitedTo(outputStream)
    }

    outputStream.toByteArray
  }
}
