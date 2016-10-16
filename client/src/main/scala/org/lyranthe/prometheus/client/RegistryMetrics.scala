package org.lyranthe.prometheus.client

case class RegistryMetrics(name: Option[MetricName],
                           help: String,
                           collectorType: String,
                           metrics: List[RegistryMetric])
