package org.lyranthe.prometheus.client

case class RegistryMetrics(name: String, help: String, collectorType: String, metrics: List[RegistryMetric])

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
      sb.append(s"# HELP ${metric.name} ${metric.help}\n")
      sb.append(s"# TYPE ${metric.name} ${metric.collectorType}\n")
      metric.metrics foreach { rm =>
        sb.append(rm.name)
        sb.append(labelsToString(rm.labels))
        sb.append(" ")
        sb.append(rm.value)
        sb.append("\n")
      }
    }

    sb.toString()
  }
}
