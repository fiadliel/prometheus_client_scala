package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.MetricName

case class RegistryMetrics(name: Option[MetricName], help: String, collectorType: String, metrics: List[RegistryMetric])
