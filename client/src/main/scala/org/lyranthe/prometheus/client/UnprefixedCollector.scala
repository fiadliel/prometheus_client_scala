package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Collector

trait UnprefixedCollector extends Collector {
  final val underlyingName: Option[MetricName] = None
}
