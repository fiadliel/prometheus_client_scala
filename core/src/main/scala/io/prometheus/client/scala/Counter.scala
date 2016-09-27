package io.prometheus.client.scala

import io.prometheus.client.scala.internal.counter._

object Counter {
  def apply(name: String, help: String): UnlabelledCounter = UnlabelledCounter(name, help)
}
