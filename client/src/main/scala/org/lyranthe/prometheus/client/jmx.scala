package org.lyranthe.prometheus.client

import java.lang.management._

import scala.collection.JavaConverters._

object jmx {
  private val clBean = ManagementFactory.getClassLoadingMXBean
  //val cBean          = ManagementFactory.getCompilationMXBean
  val gcBeans = ManagementFactory.getGarbageCollectorMXBeans.asScala.toList
  //val memManagerBean = ManagementFactory.getMemoryManagerMXBeans
  private val memBean = ManagementFactory.getMemoryMXBean
  //val memPoolBean    = ManagementFactory.getMemoryPoolMXBeans
  //val osBean         = ManagementFactory.getOperatingSystemMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val threadBean  = ManagementFactory.getThreadMXBean

  val gcUsage = new Collector {
    override def name: String = "jvm_gc_stats"

    override def help: String = "JVM Garbage Collector Statistics"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      gcBeans flatMap { bean =>
        val nameTuple = "name" -> bean.getName
        List(
          RegistryMetric(name, List(nameTuple, "type" -> "count"), bean.getCollectionCount),
          RegistryMetric(name, List(nameTuple, "type" -> "time"), bean.getCollectionTime)
        )
      }
    }
  }

  val memUsage = new Collector {
    override def name: String = "jvm_memory_usage"

    override def help: String = "JVM Memory Usage"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      def metrics(region: String, memUsage: MemoryUsage): List[RegistryMetric] = {
        def metric(memType: String, memStatistic: Long): RegistryMetric =
          RegistryMetric(name, List("region" -> region, "type" -> memType), memStatistic)

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

  val classLoader = new Collector {
    override def name: String = "jvm_classloader"

    override def help: String = "JVM Classloader statistics"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      List(
        RegistryMetric(name, List("classloader" -> "loaded"), clBean.getLoadedClassCount),
        RegistryMetric(name, List("classloader" -> "total-loaded"), clBean.getTotalLoadedClassCount),
        RegistryMetric(name, List("classloader" -> "unloaded"), clBean.getUnloadedClassCount)
      )
    }
  }

  val startTime = new Collector {
    override def name: String = "jvm_start_time"

    override def help: String = "JVM Start Time"

    override def collectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      List(RegistryMetric(name, List.empty, runtimeBean.getStartTime.toDouble / 1e3))
    }
  }

  val threadData = new Collector {
    override def name: String = "jvm_threads"

    override def help: String = "JVM Thread Information"

    override def collectorType: CollectorType = CollectorType.Gauge

    override def collect(): List[RegistryMetric] = {
      val daemon = threadBean.getDaemonThreadCount
      val all    = threadBean.getThreadCount
      List(
        RegistryMetric(name, List("type" -> "non-daemon"), all - daemon),
        RegistryMetric(name, List("type" -> "daemon"), daemon)
      )
    }
  }

  def register()(implicit registry: Registry): Unit = {
    gcUsage.register
    memUsage.register
    classLoader.register
    startTime.register
    threadData.register
  }
}
