package org.lyranthe.prometheus.client.scala

import org.lyranthe.prometheus.client.scala.internal.DefaultRegistry

trait Registry {
  def register(c: Collector): Unit
  def collect(): List[RegistryMetric]
  override def toString: String = {
    def labelsToString(labels: List[(String, String)]) = {
      if (labels.isEmpty)
        ""
      else
        labels.map { case (label, metric) => s"""$label="$metric"""" }.mkString("{", ",", "}")
    }

    val sb = new StringBuilder

    collect().foreach { rm =>
      sb.append(rm.name)
      sb.append(labelsToString(rm.labels))
      sb.append(" ")
      sb.append(rm.value)
      sb.append("\n")
    }

    sb.toString()
  }
}

object Registry {
  implicit val defaultRegistry: Registry = new DefaultRegistry
}
