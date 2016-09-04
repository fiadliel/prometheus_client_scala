package benchmarks

import io.prometheus.client.scala.Counter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class ScalaBenchmark {
  implicit val scalaCounter = Counter.create("test")("a", "b", "c")

  @Benchmark
  def inc(): Unit = {
    Counter.lookup("test")("a", "b", "c").inc("a", "b", "c")
  }

  @Benchmark
  def incManyLabelValues(): Unit = {
    val aVal = Random.nextPrintableChar().toString
    val bVal = Random.nextPrintableChar().toString
    val cVal = Random.nextPrintableChar().toString
    Counter.lookup("test")("a", "b", "c").inc(aVal, bVal, cVal)
  }

}
