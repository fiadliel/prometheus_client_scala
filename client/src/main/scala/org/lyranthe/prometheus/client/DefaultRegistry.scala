package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.registry._

class DefaultRegistry extends Registry {
  @volatile private[client] var collectors: Vector[MetricFamily] = Vector.empty

  override def unsafeRegister(c: MetricFamily): Unit = {
    require(collectors.forall(_.name.name != c.name.name),
            s"Duplicate collector with prefix ${c.name.name}")
    collectors = (collectors :+ c).sortBy(_.name.name)
  }

  override def collect(): Iterator[RegistryMetrics] = {
    collectors.toIterator.map { c =>
      RegistryMetrics(c.name, c.help, c.metricType, c.collect())
    }
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
