package io.prometheus.client.scala.internal

import java.util.concurrent.locks.ReentrantReadWriteLock

import io.prometheus.client.scala.{Collector, Registry, RegistryMetric}

class DefaultRegistry extends Registry {
  val rwLock = new ReentrantReadWriteLock
  var collectors: Vector[Collector[String]] = Vector.empty

  override def register(c: Collector[String]): Unit = {
    rwLock.writeLock().lock()
    try {
      collectors = (collectors :+ c).distinct.sortBy(_.name)
    } finally {
      rwLock.writeLock().unlock()
    }
  }

  override def collect(): List[RegistryMetric] = {
    rwLock.readLock().lock()
    try {
      collectors.foldLeft(List.empty[RegistryMetric])((metrics: List[RegistryMetric], c: Collector[_]) => metrics ++ c.collect())
    } finally {
      rwLock.readLock().unlock()
    }
  }
}
