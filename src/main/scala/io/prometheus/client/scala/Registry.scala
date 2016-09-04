package io.prometheus.client.scala

import java.util.concurrent.locks.ReentrantReadWriteLock

trait Registry {
  def register(c: Collector[String]): Unit
  def collect(): Seq[RegistryMetric]
}

object Registry {
  val defaultRegistry: Registry = new Registry {
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

    override def collect(): Vector[RegistryMetric] = {
      rwLock.readLock().lock()
      try {
        collectors.foldLeft(Vector.empty[RegistryMetric])((metrics: Vector[RegistryMetric], c: Collector[_]) => metrics ++ c.collect())
      } finally {
        rwLock.readLock().unlock()
      }
    }
  }
}
