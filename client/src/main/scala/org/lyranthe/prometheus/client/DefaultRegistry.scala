package org.lyranthe.prometheus.client

import java.util.concurrent.locks.ReentrantReadWriteLock

import org.lyranthe.prometheus.client.internal.Collector

class DefaultRegistry extends Registry {
  private[this] val rwLock                          = new ReentrantReadWriteLock
  private[client] var collectors: Vector[Collector] = Vector.empty

  override def register(c: Collector): Unit = {
    rwLock.writeLock().lock()
    try {
      // If collector with same identity is already registered, replace it
      // This is a situation that may happen with Guice/etc. (bleh)
      collectors = (collectors.filterNot(_ == c) :+ c).sortBy(_.underlyingName)
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
