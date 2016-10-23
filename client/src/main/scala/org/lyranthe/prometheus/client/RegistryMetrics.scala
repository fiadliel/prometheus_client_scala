package org.lyranthe.prometheus.client

import org.lyranthe.prometheus.client.internal.Metric

case class RegistryMetrics(name: MetricName, help: String, metricType: MetricType, metrics: List[Metric])
