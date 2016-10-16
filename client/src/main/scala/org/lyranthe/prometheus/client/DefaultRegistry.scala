package org.lyranthe.prometheus.client

import java.util.concurrent.locks.ReentrantReadWriteLock

import org.lyranthe.prometheus.client.internal.Collector

class DefaultRegistry extends Registry {
  private[this] val rwLock                          = new ReentrantReadWriteLock
  private[client] var collectors: Vector[Collector] = Vector.empty

  override def unsafeRegister(c: Collector): Unit = {
    rwLock.writeLock().lock()
    try {
      require(collectors.forall(_.underlyingName != c.underlyingName || c.underlyingName.isEmpty),
              s"Duplicate collector with prefix ${c.underlyingName.get}")

      collectors = (collectors :+ c).sortBy(_.underlyingName.map(_.name))
    } finally {
      rwLock.writeLock().unlock()
    }
  }

  override def collect(): List[RegistryMetrics] = {
    rwLock.readLock().lock()
    try {
      collectors.foldLeft(List.empty[RegistryMetrics])({
        case (metrics, c) =>
          RegistryMetrics(c.underlyingName, c.help, c.collectorType.toString, c.collect()) :: metrics
      })
    } finally {
      rwLock.readLock().unlock()
    }
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
