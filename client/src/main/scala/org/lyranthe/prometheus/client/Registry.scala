package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Collector

trait Registry {
  def register(c: Collector): Unit
  def collect(): List[RegistryMetrics]
  override def toString: String = {
    def labelsToString(labels: List[(String, String)]) = {
      if (labels.isEmpty)
        ""
      else
        labels.map { case (label, metric) => s"""$label="$metric"""" }.mkString("{", ",", "}")
    }

    val sb = new StringBuilder

    collect().foreach { metric =>
      if (metric.name.isDefined) {
        sb.append(s"# HELP ${metric.name.get} ${metric.help}\n")
        sb.append(s"# TYPE ${metric.name.get} ${metric.collectorType}\n")
      }
      metric.metrics foreach { rm =>
        (metric.name, rm.suffix) match {
          case (Some(m), Some(s)) =>
            sb.append(s"${m}_$s")
          case (Some(m), None) =>
            sb.append(m)
          case (None, Some(s)) =>
            sb.append(s"# TYPE $s ${metric.collectorType}\n")
            sb.append(s)
          case (None, None) =>
        }
        sb.append(labelsToString(rm.labels))
        sb.append(" ")
        sb.append(rm.value)
        sb.append("\n")
      }
    }

    sb.toString()
  }
}
