package org.lyranthe.prometheus.client

case class RegistryMetrics(name: Option[String], help: String, collectorType: String, metrics: List[RegistryMetric])
