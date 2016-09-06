package benchmarks

import prometheus.shapeless._
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class ShapelessBenchmark {
  implicit val scalaCounter = Counter("test")("a", "b", "c")

  @Benchmark
  def inc(): Unit = {
    scalaCounter.labelValues("a", "b", "c").inc
  }

  @Benchmark
  def incManyLabelValues(): Unit = {
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    scalaCounter.labelValues(aVal, bVal, cVal).inc
  }

}
