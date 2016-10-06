package benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import org.lyranthe.prometheus.client.scala.Counter
import scala.util.Random

@State(Scope.Benchmark)
class ScalaBenchmark {
  val scalaCounter = Counter("test", "help").labels("a", "b", "c")

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
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    val child = scalaCounter.labelValues(aVal, bVal, cVal)

    var i = 0
    while (i < 100) {
      child.inc()
      i += 1
    }
  }

}
