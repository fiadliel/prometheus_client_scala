package org.lyranthe.prometheus.client

import java.util.concurrent.locks.ReentrantReadWriteLock

class DefaultRegistry extends Registry {
  private[this] val rwLock                             = new ReentrantReadWriteLock
  private[client] var collectors: Vector[MetricFamily] = Vector.empty

  override def unsafeRegister(c: MetricFamily): Unit = {
    rwLock.writeLock().lock()
    try {
      require(collectors.forall(_.name.name != c.name.name), s"Duplicate collector with prefix ${c.name.name}")

      collectors = (collectors :+ c).sortBy(_.name.name)
    } finally {
      rwLock.writeLock().unlock()
    }
  }

  override def collect(): List[RegistryMetrics] = {
    rwLock.readLock().lock()
    try {
      collectors.foldLeft(List.empty[RegistryMetrics])({
        case (metrics, c) =>
          RegistryMetrics(c.name, c.help, c.metricType, c.collect()) :: metrics
      })
    } finally {
      rwLock.readLock().unlock()
    }
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
