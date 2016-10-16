package org.lyranthe.prometheus.client

import java.lang.management._

import scala.collection.JavaConverters._

object jmx {
  private val clBean      = ManagementFactory.getClassLoadingMXBean
  private val gcBeans     = ManagementFactory.getGarbageCollectorMXBeans.asScala.toList
  private val memBean     = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val threadBean  = ManagementFactory.getThreadMXBean

  val gcUsage = new PrefixedCollector {
    override def name: MetricName = metric"jvm_gc_stats"

    override def help: String = "JVM Garbage Collector Statistics"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      gcBeans flatMap { bean =>
        val nameTuple = label"name" -> bean.getName
        List(
          RegistryMetric(None, List(nameTuple, label"type" -> "count"), bean.getCollectionCount),
          RegistryMetric(None, List(nameTuple, label"type" -> "time"), bean.getCollectionTime / 1e3)
        )
      }
    }
  }

  val memUsage = new PrefixedCollector {
    override def name: MetricName = metric"jvm_memory_usage"

    override def help: String = "JVM Memory Usage"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      def metrics(region: String, memUsage: MemoryUsage): List[RegistryMetric] = {
        def metric(memType: String, memStatistic: Long): RegistryMetric =
          RegistryMetric(None, List(label"region" -> region, label"type" -> memType), memStatistic)

        List(
          metric("committed", memUsage.getCommitted),
          metric("init", memUsage.getInit),
          metric("max", memUsage.getMax),
          metric("used", memUsage.getUsed)
        )
      }

      metrics("heap", memBean.getHeapMemoryUsage) ::: metrics("non-heap", memBean.getNonHeapMemoryUsage)
    }
  }

  val classLoader = new PrefixedCollector {
    override def name: MetricName = metric"jvm_classloader"

    override def help: String = "JVM Classloader statistics"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      List(
        RegistryMetric(None, List(label"classloader" -> "loaded"), clBean.getLoadedClassCount),
        RegistryMetric(None, List(label"classloader" -> "total-loaded"), clBean.getTotalLoadedClassCount),
        RegistryMetric(None, List(label"classloader" -> "unloaded"), clBean.getUnloadedClassCount)
      )
    }
  }

  val startTime = new PrefixedCollector {
    override def name: MetricName = metric"jvm_start_time"

    override def help: String = "JVM Start Time"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      List(RegistryMetric(None, List.empty, runtimeBean.getStartTime.toDouble / 1e3))
    }
  }

  val threadData = new PrefixedCollector {
    override def name: MetricName = metric"jvm_threads"

    override def help: String = "JVM Thread Information"

    override def collectorType: CollectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      val daemon = threadBean.getDaemonThreadCount
      val all    = threadBean.getThreadCount
      List(
        RegistryMetric(None, List(label"type" -> "non-daemon"), all - daemon),
        RegistryMetric(None, List(label"type" -> "daemon"), daemon)
      )
    }
  }

  def unsafeRegister()(implicit registry: Registry): Unit = {
    gcUsage.unsafeRegister
    memUsage.unsafeRegister
    classLoader.unsafeRegister
    startTime.unsafeRegister
    threadData.unsafeRegister
  }
}
