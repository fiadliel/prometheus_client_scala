package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

class DefaultRegistry extends Registry {
  @volatile
  private[client] var metricFamilies: Vector[MetricFamily] = Vector.empty

  @volatile
  private[client] var collectors: Vector[Collector] = Vector.empty

  override def register(collector: Collector): Boolean = {
    if (!collectors.contains(collector)) {
      collectors = collectors :+ collector
      true
    } else {
      false
    }
  }

  override def register(c: MetricFamily): Boolean = {
    if (metricFamilies.forall(_.name.name != c.name.name)) {
      metricFamilies = (metricFamilies :+ c).sortBy(_.name.name)
      true
    } else
      false
  }

  override def collect(): Iterator[RegistryMetrics] = {
    metricFamilies.toIterator.map { c =>
      RegistryMetrics(c.name, c.help, c.escapedHelp, c.metricType, c.collect())
    } ++ collectors.flatMap(_.collect())
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
