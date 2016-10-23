package benchmarks

import io.prometheus.client.{Counter, Histogram}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class JavaBenchmark {
  val javaCounter = Counter
    .build()
    .name("test")
    .help("help")
    .labelNames("a", "b", "c")
    .create()
  val javaHistogram = Histogram
    .build()
    .name("testhist")
    .help("help")
    .buckets(0.001,
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
    .labelNames("a", "b", "c")
    .create()

  @Benchmark
  def inc(): Unit = {
    javaCounter.labels("a", "b", "c").inc()
  }

  @Benchmark
  def incManyLabelValues(): Unit = {
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    javaCounter.labels(aVal, bVal, cVal).inc()
  }

  @Benchmark
  def incManyLabelValuesRepeatedly(): Unit = {
    val aVal  = Random.nextPrintableChar().toString
    val bVal  = Random.nextPrintableChar().toString
    val cVal  = Random.nextPrintableChar().toString
    val child = javaCounter.labels(aVal, bVal, cVal)

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
    val child = javaHistogram.labels(aVal, bVal, cVal)

    var i = 0
    while (i < 100) {
      child.observe(Random.nextDouble() * 6)
      i += 1
    }
  }

}
