package benchmarks

import io.prometheus.client.Counter
import io.prometheus.client.scala.{ Counter => SCounter }
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class CounterBenchmark {
  val javaCounter = Counter.build().name("test").help("help").labelNames("a", "b", "c").create()
  implicit val scalaCounter = SCounter.create("test", "a", "b", "c")

  @Benchmark
  def incJava(): Unit = {
    javaCounter.labels("a", "b", "c").inc()
  }

  @Benchmark
  def incScala(): Unit = {
    SCounter.lookup("test", "a", "b", "c").inc("a", "b", "c")
  }
}
