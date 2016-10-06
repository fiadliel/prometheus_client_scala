package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.counter._

object Counter {
  def apply(name: String, help: String): UnlabelledCounter =
    UnlabelledCounter(name, help)
}
