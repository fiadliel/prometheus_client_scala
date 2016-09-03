package io.prometheus.client.scala

trait Registry {
  def register(c: Collector[_]): Unit
}

//package prometheus_client
//
//import java.util.concurrent.atomic.DoubleAdder
//
//import shapeless._
//import shapeless.syntax.singleton._
//
//object Stuff {
//  val ValidMetricName = "[a-zA-Z_:][a-zA-Z0-9_:]*".r
//  val ValidLabel = "[a-zA-Z_][a-zA-Z0-9_]*".r
//}
//
//trait Counter {
//  def help: String
//}
//
//trait Counter0[N <: String] extends Counter {
//  def inc(): Unit
//}
//
//trait Counter1[N <: String, L1 <: String] extends Counter {
//  def inc(l1: String): Unit
//}
//
//object Other {
//  implicit val myCounter = new Counter0["name"] {
//    val name = "name".narrow
//    val help = "help"
//  }
//}


//case class MetricData[A <: Collector, B <: Metrics](collector: A, metrics: B)
//
//sealed trait Metrics extends Product with Serializable
//case class CounterMetrics(value: Double, labels: Vector[String]) extends Metrics
//case class GaugeMetrics(value: Double, labels: Vector[String]) extends Metrics
//case class HistogramMetrics(values: Seq[(Double, Vector[String])]) extends Metrics
//
//trait Collector {
//  def name: String
//  def help: String
//  def labels: Seq[String]
//}
//
//
//trait Histogram extends Collector {
//  def observe(v: Double): Unit
//
//  def collect: MetricData[Histogram, HistogramMetrics]
//}
//
//package impl {
//
//  class CounterImpl(val name: String, val help: String, val labels: Vector[String]) extends Counter {
//    val adder = new DoubleAdder
//
//    override def inc(): Unit = adder.add(1D)
//    override def inc(v: Double): Unit = adder.add(v)
//    override def collect: MetricData[Counter, CounterMetrics] = MetricData(this, CounterMetrics(adder.sum(), labels))
//  }
//
//}
