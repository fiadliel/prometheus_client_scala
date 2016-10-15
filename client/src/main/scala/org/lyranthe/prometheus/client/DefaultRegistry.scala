package org.lyranthe.prometheus.client

import java.util.concurrent.locks.ReentrantReadWriteLock

class DefaultRegistry extends Registry {
  val rwLock                        = new ReentrantReadWriteLock
  var collectors: Vector[Collector] = Vector.empty

  override def register(c: Collector): Unit = {
    rwLock.writeLock().lock()
    try {
      // If collector with same identity is already registered, replace it
      // This is a situation that may happen with Guice/etc. (bleh)
      collectors = (collectors.filterNot(_ == c) :+ c).sortBy(_.name)
    } finally {
      rwLock.writeLock().unlock()
    }
  }

  override def collect(): List[RegistryMetrics] = {
    rwLock.readLock().lock()
    try {
      collectors.foldLeft(List.empty[RegistryMetrics])({
        case (metrics, c) =>
          RegistryMetrics(c.name, c.help, c.collectorType.toString, c.collect()) :: metrics
      })
    } finally {
      rwLock.readLock().unlock()
    }
  }
}

object DefaultRegistry {
  def apply(): DefaultRegistry = new DefaultRegistry
}
