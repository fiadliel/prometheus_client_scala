package io.prometheus.client.scala

trait Registry {
  def register(c: Collector[_]): Unit
}

object Registry {
  val defaultRegistry: Registry = new Registry {
    override def register(c: Collector[_]): Unit = ???
  }
}
