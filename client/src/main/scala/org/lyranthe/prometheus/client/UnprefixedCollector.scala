package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.{Collector, MetricName}

trait UnprefixedCollector extends Collector {
  final val underlyingName: Option[MetricName] = None
}
