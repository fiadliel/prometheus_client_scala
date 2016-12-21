package org.lyranthe.prometheus.client

import java.lang.management._

import org.lyranthe.prometheus.client.registry._

import scala.collection.JavaConverters._

object jmx {
  private val clBean = ManagementFactory.getClassLoadingMXBean
  private val gcBeans =
    ManagementFactory.getGarbageCollectorMXBeans.asScala.toList
  private val memBean     = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val threadBean  = ManagementFactory.getThreadMXBean

  val gcUsage = new MetricFamily {
    override def name: MetricName = metric"jvm_gc_stats"

    override def help: String = "JVM Garbage Collector Statistics"
    override def escapedHelp: String = help

    override def metricType = MetricType.Gauge

    override def collect(): List[Metric] = {
      gcBeans flatMap { bean =>
        val nameTuple = LabelPair(label"name", bean.getName)
        List(
          GaugeMetric(List(nameTuple, LabelPair(label"type", "count")),
                      bean.getCollectionCount),
          GaugeMetric(List(nameTuple, LabelPair(label"type", "time")),
                      bean.getCollectionTime / 1e3)
        )
      }
    }
  }

  val memUsage = new MetricFamily {
    override def name: MetricName = metric"jvm_memory_usage"

    override def help: String = "JVM Memory Usage"
    override def escapedHelp: String = help

    override def metricType = MetricType.Gauge

    override def collect(): List[Metric] = {
      def metrics(region: String, memUsage: MemoryUsage): List[GaugeMetric] = {
        def metric(memType: String, memStatistic: Long): GaugeMetric =
          GaugeMetric(List(LabelPair(label"region", region),
                           LabelPair(label"type", memType)),
                      memStatistic)

        List(
          metric("committed", memUsage.getCommitted),
          metric("init", memUsage.getInit),
          metric("max", memUsage.getMax),
          metric("used", memUsage.getUsed)
        )
      }

      metrics("heap", memBean.getHeapMemoryUsage) ::: metrics(
        "non-heap",
        memBean.getNonHeapMemoryUsage)
    }
  }

  val classLoader = new MetricFamily {
    override def name: MetricName = metric"jvm_classloader"

    override def help: String = "JVM Classloader statistics"
    override def escapedHelp: String = help

    override def metricType = MetricType.Gauge

    override def collect(): List[Metric] = {
      List(
        GaugeMetric(List(LabelPair(label"classloader", "loaded")),
                    clBean.getLoadedClassCount),
        GaugeMetric(List(LabelPair(label"classloader", "total-loaded")),
                    clBean.getTotalLoadedClassCount),
        GaugeMetric(List(LabelPair(label"classloader", "unloaded")),
                    clBean.getUnloadedClassCount)
      )
    }
  }

  val startTime = new MetricFamily {
    override def name: MetricName = metric"jvm_start_time"

    override def help: String = "JVM Start Time"
    override def escapedHelp: String = help

    override def metricType = MetricType.Gauge

    override def collect(): List[Metric] = {
      List(GaugeMetric(List.empty, runtimeBean.getStartTime.toDouble / 1e3))
    }
  }

  val threadData = new MetricFamily {
    override def name: MetricName = metric"jvm_threads"

    override def help: String = "JVM Thread Information"
    override def escapedHelp: String = help

    override def metricType: MetricType = MetricType.Gauge

    override def collect(): List[Metric] = {
      val daemon = threadBean.getDaemonThreadCount
      val all    = threadBean.getThreadCount
      List(
        GaugeMetric(List(LabelPair(label"type", "non-daemon")), all - daemon),
        GaugeMetric(List(LabelPair(label"type", "daemon")), daemon)
      )
    }
  }

  def register()(implicit registry: Registry): Boolean = {
    val results = Vector(
      gcUsage.register,
      memUsage.register,
      classLoader.register,
      startTime.register,
      threadData.register
    )

    results.forall(_ == true)
  }
}
