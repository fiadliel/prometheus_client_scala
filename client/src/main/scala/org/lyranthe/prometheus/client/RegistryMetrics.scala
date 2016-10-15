package org.lyranthe.prometheus.client

case class RegistryMetrics(name: String, help: String, collectorType: String, metrics: List[RegistryMetric])
