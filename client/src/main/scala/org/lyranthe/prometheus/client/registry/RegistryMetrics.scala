package org.lyranthe.prometheus.client.registry

import org.lyranthe.prometheus.client.{MetricName, MetricType}

case class RegistryMetrics(name: MetricName, help: String, metricType: MetricType, metrics: List[Metric])
