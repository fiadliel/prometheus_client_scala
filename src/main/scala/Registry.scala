package io.prometheus.client.scala

trait Registry {
  def register(c: Collector[_]): Unit
}
