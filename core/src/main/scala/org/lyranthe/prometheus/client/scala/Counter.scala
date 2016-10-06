package org.lyranthe.prometheus.client.scala

import org.lyranthe.prometheus.client.scala.internal.counter._

object Counter {
  def apply(name: String, help: String): UnlabelledCounter =
    UnlabelledCounter(name, help)
}
