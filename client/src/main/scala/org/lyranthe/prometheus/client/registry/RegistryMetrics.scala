package org.lyranthe.prometheus.client.registry

import org.lyranthe.prometheus.client.{MetricName, MetricType}

case class RegistryMetrics(name: MetricName,
                           help: String,
                           escapedHelp: String,
                           metricType: MetricType,
                           metrics: List[Metric])
