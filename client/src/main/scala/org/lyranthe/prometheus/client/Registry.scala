package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Collector

trait Registry {
  def unsafeRegister(c: Collector): Unit
  def collect(): List[RegistryMetrics]
  override def toString: String = {
    def labelsToString(labels: List[(LabelName, String)]) = {
      if (labels.isEmpty)
        ""
      else
        labels.map { case (label, metric) => s"""${label.name}="$metric"""" }.mkString("{", ",", "}")
    }

    val sb = new StringBuilder

    collect().foreach { metric =>
      if (metric.name.isDefined) {
        sb.append(s"# HELP ${metric.name.get.name} ${metric.help}\n")
        sb.append(s"# TYPE ${metric.name.get.name} ${metric.collectorType}\n")
      }
      metric.metrics foreach { rm =>
        (metric.name, rm.suffix) match {
          case (Some(m), Some(s)) =>
            sb.append(s"${m.name}_$s")
          case (Some(m), None) =>
            sb.append(m.name)
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
