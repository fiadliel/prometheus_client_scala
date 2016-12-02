package org.lyranthe.prometheus.client.registry

import java.io.ByteArrayOutputStream

import org.lyranthe.prometheus.client._

object TextFormat extends RegistryFormat {
  override val contentType = "text/plain; version=0.0.4"

  def prometheusDoubleFormat(d: Double): String = {
    if (d == Double.PositiveInfinity)
      "+Inf"
    else if (d == Double.NegativeInfinity)
      "-Inf"
    else if (d.isNaN)
      "NaN"
    else
      d.toString
  }

  def output(values: => Iterator[RegistryMetrics]): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream(4096)

    def labelsToString(labels: List[LabelPair]) = {
      if (labels.isEmpty)
        ""
      else
        labels.map {
          case LabelPair(label, metric) => s"""${label.name}="$metric""""
        }.mkString("{", ",", "}")
    }

    values.foreach { metric =>
      val sb = new StringBuilder
      sb.append(s"# HELP ${metric.name.name} ${metric.help}\n")
      sb.append(s"# TYPE ${metric.name.name} ${metric.metricType.toString}\n")
      metric.metrics foreach { rm =>
        rm match {
          case GaugeMetric(labels, value) =>
            sb.append(
              s"${metric.name.name}${labelsToString(labels)} ${value}\n")
          case CounterMetric(labels, value) =>
            sb.append(
              s"${metric.name.name}${labelsToString(labels)} ${value}\n")
          case HistogramMetric(labels, sampleCount, sampleSum, buckets) =>
            val labelStr = labelsToString(labels)
            buckets foreach { bucket =>
              sb.append(s"${metric.name.name}_bucket${labelsToString(
                LabelPair(label"le", prometheusDoubleFormat(bucket.upperBound)) :: labels)} ${bucket.cumulativeCount}\n")
            }
            sb.append(s"${metric.name.name}_count${labelStr} ${sampleCount}\n")
            sb.append(s"${metric.name.name}_sum${labelStr} ${sampleSum}\n")
        }
      }
      outputStream.write(sb.toString.getBytes)
    }

    outputStream.toByteArray
  }
}
