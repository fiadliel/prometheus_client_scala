package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.{CounterMetric, GaugeMetric, HistogramMetric}

trait Registry {
  def unsafeRegister(c: MetricFamily): Unit
  def collect(): Iterator[RegistryMetrics]

  override def toString: String = {
    def labelsToString(labels: List[(LabelName, String)]) = {
      if (labels.isEmpty)
        ""
      else
        labels.map { case (label, metric) => s"""${label.name}="$metric"""" }.mkString("{", ",", "}")
    }

    val sb = new StringBuilder

    collect().foreach { metric =>
      sb.append(s"# HELP ${metric.name.name} ${metric.help}\n")
      sb.append(s"# TYPE ${metric.name.name} ${metric.metricType.toString}\n")
      metric.metrics foreach { rm =>
        rm match {
          case GaugeMetric(labels, value) =>
            sb.append(s"${metric.name.name}${labelsToString(labels)} ${value}\n")
          case CounterMetric(labels, value) =>
            sb.append(s"${metric.name.name}${labelsToString(labels)} ${value}\n")
          case HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
            val labelStr = labelsToString(labels)
            buckets foreach { bucket =>
              sb.append(s"${metric.name.name}_bucket${labelsToString(label"le" -> HistogramBuckets
                .prometheusDoubleFormat(bucket.upperBound) :: labels)} ${bucket.cumulativeCount}\n")
            }
            sb.append(s"${metric.name.name}_count${labelStr} ${sampleCount}\n")
            sb.append(s"${metric.name.name}_sum${labelStr} ${sampleSum}\n")
        }
      }
    }

    sb.toString()
  }
}
