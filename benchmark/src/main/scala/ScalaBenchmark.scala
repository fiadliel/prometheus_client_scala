package benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import io.prometheus.client.scala.Counter
import scala.util.Random

@State(Scope.Benchmark)
class ScalaBenchmark {
  val scalaCounter = Counter("test", "a", "b", "c")

  @Benchmark
  def inc(): Unit = {
    scalaCounter.inc("a", "b", "c")
  }

  @Benchmark
  def incManyLabelValues(): Unit = {
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    scalaCounter.inc(aVal, bVal, cVal)
  }

}
