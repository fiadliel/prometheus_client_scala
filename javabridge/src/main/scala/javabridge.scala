package org.lyranthe.prometheus.client

import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.CollectorRegistry
import org.lyranthe.prometheus.client.registry._

import scala.collection.JavaConverters._

object javabridge {
  val convertType: io.prometheus.client.Collector.Type => MetricType = {
    case io.prometheus.client.Collector.Type.COUNTER   => MetricType.Counter
    case io.prometheus.client.Collector.Type.GAUGE     => MetricType.Gauge
    case io.prometheus.client.Collector.Type.HISTOGRAM => MetricType.Histogram
    case io.prometheus.client.Collector.Type.SUMMARY   => MetricType.Summary
    case io.prometheus.client.Collector.Type.UNTYPED   => MetricType.Untyped
  }

  private def convertSample(t: MetricType,
                            sample: MetricFamilySamples.Sample): Metric = {
    val labelNames  = sample.labelNames.asScala
    val labelValues = sample.labelValues.asScala

    val name  = sample.name
    val value = sample.value

    val labels = (labelNames zip labelValues).toList map {
      case (n, v) => LabelPair(LabelName.unsafeFromString(n), v)
    }

    t match {
      case MetricType.Counter =>
        CounterMetric(labels, value)
      case MetricType.Gauge =>
        GaugeMetric(labels, value)
      case MetricType.Histogram =>
        HistogramMetric(labels, ???, ???, ???)
      case MetricType.Summary =>
        SummaryMetric(labels, ???, ???, ???)
      case MetricType.Untyped =>
        UntypedMetric(labels, value)
    }
  }

  private def convertPrometheusStringToDouble(s: String): Option[Double] = {
    if (s == "+Inf")
      Some(Double.MaxValue)
    else if (s == "-Inf")
      Some(Double.MinValue)
    else if (s == "NaN")
      Some(Double.NaN)
    else
      try {
        Some(s.toDouble)
      } catch {
        case e: NumberFormatException => None
      }
  }

  private def convertHistogram(
      samples: Seq[MetricFamilySamples.Sample]): List[Metric] = {
    ???
//    val convertedSamples = samples.flatMap { sample =>
//      val labelPairs = sample.labelNames.asScala zip sample.labelValues.asScala
//      val bucket     = labelPairs.find(_._1 == "le").map(_._2)
//
//      bucket.map { b =>
//        val labels = labelPairs.filterNot(_._1 == "le").map {
//          case (name, v) => LabelPair(LabelName.unsafeFromString(name), v)
//        }
//        (sample.name, labels, b, sample.value)
//      }
//    }
//
//    val sums = convertedSamples.filter(_._1.endsWith("_sum")).groupBy {
//      sample =>
//        (sample._1.substring(sample._1.length - 4, sample._1.length),
//         sample._2)
//    }
//
//    val counts = convertedSamples.filter(_._1.endsWith("_count")).groupBy {
//      sample =>
//        (sample._1.substring(sample._1.length - 6, sample._1.length),
//         sample._2)
//    }
//    val buckets = convertedSamples.filter(_._1.endsWith("_bucket")).groupBy {
//      sample =>
//        (sample._1.substring(sample._1.length - 7, sample._1.length),
//         sample._2)
//    }
//
//    val result = for {
//      key <- sums.keys
//      metricSum = sums(key).head._4
//      metricCount <- counts.get(key).map(_.head._4.toLong)
//      metricBuckets <- buckets
//        .get(key)
//        .map(
//          _.map(kv => kv._3 -> kv._4)
//            .flatMap(kv =>
//              convertPrometheusStringToLong(kv._1).map(v => Bucket(v, kv._2)))
//            .toArray)
//      labelPairs = key._2.toList
//    } yield HistogramMetric(labelPairs, metricCount, metricSum, metricBuckets)
//
//    result.toList
  }

  private def convertSummary(
      samples: Seq[MetricFamilySamples.Sample]): List[Metric] = {
    println(samples)
    val convertedSamples = samples.map { sample =>
      val labelPairs = sample.labelNames.asScala zip sample.labelValues.asScala
      val bucket = labelPairs
        .find(_._1 == "quantile")
        .flatMap(v => convertPrometheusStringToDouble(v._2))

      val labels = labelPairs.filterNot(_._1 == "quantile").map {
        case (name, v) => LabelPair(LabelName.unsafeFromString(name), v)
      }
      (sample.name, labels, bucket, sample.value)
    }

    val sums = convertedSamples.filter(_._1.endsWith("_sum")).groupBy {
      sample =>
        (sample._1.substring(0, sample._1.length - 4), sample._2)
    }

    val counts = convertedSamples.filter(_._1.endsWith("_count")).groupBy {
      sample =>
        (sample._1.substring(0, sample._1.length - 6), sample._2)
    }
    val quantiles = convertedSamples
      .collect {
        case (name, labels, Some(bucket), sampleValue) =>
          (name, labels, bucket, sampleValue)
      }
      .groupBy { sample =>
        (sample._1, sample._2)
      }

    val result = for {
      key <- sums.keys
      metricSum = sums(key).head._4
      metricCount <- counts.get(key).map(_.head._4.toLong)
      metricQuantiles = quantiles
        .get(key)
        .toArray
        .flatMap(_.map(kv => Quantile(kv._3, kv._4)))
      labelPairs = key._2.toList
    } yield SummaryMetric(labelPairs, metricCount, metricSum, metricQuantiles)

    result.toList
  }

  private def convertSamples(
      samples: Seq[MetricFamilySamples.Sample]): MetricType => List[Metric] = {
    case MetricType.Counter =>
      samples.map(convertSample(MetricType.Counter, _))(collection.breakOut)
    case MetricType.Gauge =>
      samples.map(convertSample(MetricType.Gauge, _))(collection.breakOut)
    case MetricType.Histogram =>
      convertHistogram(samples)
    case MetricType.Summary =>
      convertSummary(samples)
    case MetricType.Untyped =>
      samples.map(convertSample(MetricType.Untyped, _))(collection.breakOut)
  }

  val javaMetrics: Collector = new Collector {
    override def collect(): Iterator[RegistryMetrics] = {
      val javaValues =
        CollectorRegistry.defaultRegistry.metricFamilySamples().asScala
      javaValues.map { samples =>
        val convertedType = convertType(samples.`type`)

        RegistryMetrics(
          MetricName(samples.name),
          samples.help,
          samples.help.replace("\\", "\\\\").replace("\n", "\\n"),
          convertedType,
          convertSamples(samples.samples.asScala)(convertedType)
        )
      }
    }
  }

  def register()(implicit registry: Registry): Boolean = {
    registry.register(javaMetrics)
  }
}
