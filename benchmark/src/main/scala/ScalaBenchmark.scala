package benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import org.lyranthe.prometheus.client._

import scala.util.Random

@State(Scope.Benchmark)
class ScalaBenchmark {
  implicit val histogramBuckets =
    HistogramBuckets(0.001,
                     0.002,
                     0.005,
                     0.01,
                     0.02,
                     0.05,
                     0.1,
                     0.2,
                     0.5,
                     1.0,
                     2.0,
                     5.0,
                     10)

  val scalaCounter =
    Counter(metric"test", "help").labels(label"a", label"b", label"c")
  val scalaHistogram =
    Histogram(metric"testhist", "help").labels(label"a", label"b", label"c")

  @Benchmark
  def inc(): Unit = {
    scalaCounter.labelValues("a", "b", "c").inc()
  }

  @Benchmark
  def incManyLabelValues(): Unit = {
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    scalaCounter.labelValues(aVal, bVal, cVal).inc()
  }

  @Benchmark
  def incManyLabelValuesRepeatedly(): Unit = {
    val aVal  = Random.nextPrintableChar().toString
    val bVal  = Random.nextPrintableChar().toString
    val cVal  = Random.nextPrintableChar().toString
    val child = scalaCounter.labelValues(aVal, bVal, cVal)

    var i = 0
    while (i < 100) {
      child.inc()
      i += 1
    }
  }

  @Benchmark
  def observeManyLabelValuesRepeatedly(): Unit = {
    val aVal  = Random.nextPrintableChar().toString
    val bVal  = Random.nextPrintableChar().toString
    val cVal  = Random.nextPrintableChar().toString
    val child = scalaHistogram.labelValues(aVal, bVal, cVal)

    var i = 0
    while (i < 100) {
      child.observe(Random.nextDouble() * 6)
      i += 1
    }
  }
}
