package blah

import com.google.protobuf.CodedOutputStream
import io.prometheus.client.Metrics._
import org.lyranthe.prometheus.client

object Protobuf {

  def labelPairs(labels: List[(client.LabelName, String)]): List[LabelPair] = {
    labels.map(lp => LabelPair.newBuilder.setName(lp._1.name).setValue(lp._2).build)
  }

  def convertBucket(bucket: client.internal.Bucket): Bucket = {
    Bucket.newBuilder.setCumulativeCount(bucket.cumulativeCount).setUpperBound(bucket.upperBound).build
  }

  def convertMetric(metric: client.internal.Metric): Metric = {
    import scala.collection.JavaConverters._

    val newMetric = Metric.newBuilder
    newMetric.addAllLabel(labelPairs(metric.labels).asJava)

    metric match {
      case client.internal.GaugeMetric(labels, value) =>
        newMetric.setGauge(Gauge.newBuilder.setValue(value))
      case client.internal.CounterMetric(labels, value) =>
        newMetric.setCounter(Counter.newBuilder.setValue(value))
      case client.internal.HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
        newMetric.setHistogram(
          Histogram.newBuilder
            .setSampleCount(sampleCount)
            .setSampleSum(sampleSum)
            .addAllBucket(buckets.map(convertBucket).toIterable.asJava))
    }

    newMetric.build
  }

  def convertMetricType(metricType: client.MetricType): MetricType = {
    metricType match {
      case client.MetricType.Counter   => MetricType.COUNTER
      case client.MetricType.Gauge     => MetricType.GAUGE
      case client.MetricType.Histogram => MetricType.HISTOGRAM
    }
  }

  def toProtobuf(registry: client.Registry): List[Array[Byte]] = {
    import scala.collection.JavaConverters._

    val metrics = registry.collect()

    val protos = metrics.map { metric =>
      MetricFamily.newBuilder
        .setName(metric.name.name)
        .setHelp(metric.help)
        .setType(convertMetricType(metric.metricType))
        .addAllMetric(metric.metrics.map(convertMetric).asJava)
        .build
    }

    protos.map { proto =>
      val serializedSize = proto.getSerializedSize
      val sizeTagSize    = CodedOutputStream.computeUInt32SizeNoTag(serializedSize)
      val arr            = new Array[Byte](sizeTagSize + serializedSize)
      val outputStream   = CodedOutputStream.newInstance(arr)

      outputStream.writeRawVarint32(serializedSize)
      proto.writeTo(outputStream)
      outputStream.flush

      arr
    }
  }
}
