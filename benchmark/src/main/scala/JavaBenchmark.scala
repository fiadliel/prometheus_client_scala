package benchmarks

import io.prometheus.client.Counter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class JavaBenchmark {
  val javaCounter = Counter.build().name("test").help("help").labelNames("a", "b", "c").create()

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

}
