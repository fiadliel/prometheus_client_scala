package org.lyranthe.prometheus.client.internal

import java.util.concurrent.locks.ReentrantReadWriteLock

import org.lyranthe.prometheus.client.{Collector, Registry, RegistryMetrics}

class DefaultRegistry extends Registry {
  val rwLock                        = new ReentrantReadWriteLock
  var collectors: Vector[Collector] = Vector.empty

  override def register(c: Collector): Unit = {
    rwLock.writeLock().lock()
    try {
      collectors = (collectors :+ c).distinct.sortBy(_.name)
    } finally {
      rwLock.writeLock().unlock()
    }
  }

  override def collect(): List[RegistryMetrics] = {
    rwLock.readLock().lock()
    try {
      collectors.foldLeft(List.empty[RegistryMetrics])({
        case (metrics, c) =>
          RegistryMetrics(c.name, c.help, c.collectorType.toString.toLowerCase, c.collect()) :: metrics
      })
    } finally {
      rwLock.readLock().unlock()
    }
  }
}
