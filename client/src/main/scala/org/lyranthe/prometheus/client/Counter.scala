package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.counter.UnlabelledCounter

object Counter {
  def apply(name: MetricName, help: String): UnlabelledCounter =
    UnlabelledCounter(name, help)
}
