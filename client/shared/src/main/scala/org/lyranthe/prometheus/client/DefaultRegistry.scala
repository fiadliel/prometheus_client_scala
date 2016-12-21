package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

class DefaultRegistry extends Registry {
  @volatile private[client] var collectors: Vector[MetricFamily] = Vector.empty

  override def register(c: MetricFamily): Boolean = {
    if (collectors.forall(_.name.name != c.name.name)) {
      collectors = (collectors :+ c).sortBy(_.name.name)
      true
    } else
      false
  }

  override def collect(): Iterator[RegistryMetrics] = {
    collectors.toIterator.map { c =>
      RegistryMetrics(c.name, c.help, c.escapedHelp, c.metricType, c.collect())
    }
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
